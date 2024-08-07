package com.tech.engg5.xml.batch.processor.utility;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.tech.engg5.xml.batch.processor.exception.S3MultiPartUploadException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.amazonaws.services.s3.internal.Constants.MB;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class S3MultiPartUpload {

  public static final int MAX_UPLOAD_NUMBER = 10_000;
  public static final int MIN_UPLOAD_PART_BYTES_SIZE = 5 * MB;
  private final AtomicInteger uploadPartNumber = new AtomicInteger(0);
  private final Config config;
  private final String bucketName;
  private final String key;
  private final ExecutorService executorService;
  private final AmazonS3 s3Client;
  private String uploadId;
  private volatile boolean isAborting = false;
  private final List<Future<PartETag>> partETagFutures = new ArrayList<>();

  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Config {

    public static final Config DEFAULT = new Config();

    @With
    private int awaitTerminationTimeSeconds = 2;

    @With
    private int threadCount = 4;

    @With
    private int queueSize = 4;

    @With
    private int uploadPartBytesLimit = 20 * MB;

    @With
    private CannedAccessControlList cannedAcl;

    @With
    private String contentType;

    @With
    private Function<InitiateMultipartUploadRequest, InitiateMultipartUploadRequest> customizeInitiateUploadRequest;
  }

  public S3MultiPartUpload(String bucketName, String key, AmazonS3 s3Client, Config config) {
    var threadPoolExecutor = new ThreadPoolExecutor(config.threadCount, config.threadCount, 0L,
      TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(config.queueSize));

    threadPoolExecutor.setRejectedExecutionHandler((r, executor) -> {
      try {
        if (!executor.isShutdown()) {
          executor.getQueue().put(r);
        }
      } catch (InterruptedException exc) {
        Thread.currentThread().interrupt();
        throw new RejectedExecutionException("Executor was interrupted while the task was waiting to be "
          + "put on the work queue", exc);
      }
    });

    this.config = config;
    this.executorService = threadPoolExecutor;
    this.bucketName = bucketName;
    this.key = key;
    this.s3Client = s3Client;
  }

  public void initialize() {
    var initRequest = new InitiateMultipartUploadRequest(bucketName, key);
    initRequest.setTagging(new ObjectTagging(new ArrayList<>()));

    var metaData = new ObjectMetadata();
    if (config.contentType != null) {
      metaData.setContentType(config.contentType);
    }
    initRequest.setObjectMetadata(metaData);

    if (config.cannedAcl != null) {
      initRequest.withCannedACL(config.cannedAcl);
    }

    if (config.customizeInitiateUploadRequest != null) {
      initRequest = config.customizeInitiateUploadRequest.apply(initRequest);
    }

    try {
      uploadId = s3Client.initiateMultipartUpload(initRequest).getUploadId();
    } catch (Throwable t) {
      LOG.error("Failed initializing multipart upload with uploadId - [{}]", uploadId);
      throw abort(t);
    }
  }

  public void uploadPart(byte[] bytes, int partNumber) {
    uploadPart(new ByteArrayInputStream(bytes), partNumber);
  }

  public void uploadPart(ByteArrayInputStream inputStream, int partNumber) {
    submitUploadPart(inputStream, partNumber, false);
  }

  public void complete() {
    try {
      int partNumber = incrementUploadNumber();
      LOG.info("Completing multipart upload. Total parts - [{}]", partNumber);
      submitUploadPart(new ByteArrayInputStream(new byte[0]), partNumber, true);
      var partETags = waitForAllUploadParts();
      s3Client.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags));
    } catch (Throwable t) {
      LOG.error("Failed to upload part.");
      throw abort(t);
    } finally {
      shutDownAndAwaitTermination();
    }
  }

  private void submitUploadPart(ByteArrayInputStream inputStream, int partNumber, boolean finalPart) {
    submitTask(() -> {
      int partSize = inputStream.available();
      var uploadPartRequest = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey(key)
        .withUploadId(uploadId)
        .withPartNumber(partNumber)
        .withPartSize(partSize)
        .withInputStream(inputStream);

      if (finalPart) {
        uploadPartRequest.withLastPart(true);
      }

      try {
        LOG.info("Submitting upload for part - [{}], uploadId - [{}], partSize - [{}]", partNumber, uploadId, partSize);
        var uploadPartResult = s3Client.uploadPart(uploadPartRequest);
        LOG.info("Submitted partNumber - [{}], uploadId - [{}]. Result - [{}]", partNumber, uploadId,
          uploadPartResult.getPartETag());
        return uploadPartResult.getPartETag();
      } catch (Throwable t) {
        throw abort(t);
      }
    });
  }

  private void submitTask(Callable<PartETag> task) {
    var partETagFuture = executorService.submit(task);
    partETagFutures.add(partETagFuture);
  }

  private List<PartETag> waitForAllUploadParts() throws InterruptedException, ExecutionException {
    List<PartETag> partETags = new ArrayList<>();
    for (var partETagFuture : partETagFutures) {
      partETags.add(partETagFuture.get());
    }
    return partETags;
  }

  private void shutDownAndAwaitTermination() {
    LOG.info("Shutting down executor service for uploadId - [{}]", uploadId);
    executorService.shutdownNow();
    try {
      executorService.awaitTermination(config.awaitTerminationTimeSeconds, TimeUnit.SECONDS);
    } catch (InterruptedException exc) {
      LOG.error("Interrupted while awaiting executor service shutdown.");
      Thread.currentThread().interrupt();
    }
    executorService.shutdownNow();
  }

  private int incrementUploadNumber() {
    int uploadNumber = uploadPartNumber.incrementAndGet();
    if (uploadNumber > MAX_UPLOAD_NUMBER) {
      throw new IllegalStateException("Upload part number cannot exceed " + MAX_UPLOAD_NUMBER);
    }
    return uploadNumber;
  }

  public RuntimeException abort(Throwable t) {
    if (!isAborting) {
      LOG.error("Aborting [{}] due to error - [{}]", this, t);
    }

    abort();

    if (t instanceof Error) {
      throw (Error) t;
    } else if (t instanceof InterruptedException) {
      Thread.currentThread().interrupt();
      throw new S3MultiPartUploadException();
    } else {
      throw new S3MultiPartUploadException("S3MultiPartUpload aborted.", t);
    }
  }

  public void abort() {
    synchronized (this) {
      if (!isAborting) {
        return;
      }
      isAborting = true;
      if (uploadId != null) {
        LOG.debug("[{}] - Aborting", this);
        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, key, uploadId));
        LOG.info("[{}] - Aborted", this);
      }
    }
  }

  public void uploadDataStream(InputStream inputStream, int bufferSize) throws IOException {
    int totalBytesRead = 0;
    int partNumber;
    byte[] bytes;
    initialize();
    while ((bytes = inputStream.readNBytes(bufferSize)).length > 0) {
      totalBytesRead += bytes.length;
      partNumber = incrementUploadNumber();
      LOG.info("Uploading part - [{}] for file - [{}]. Read [{}] bytes so far.", partNumber, key, totalBytesRead);
      uploadPart(bytes, partNumber);
    }
    complete();
    LOG.info("Data stream completed for file - [{}]. [{}] bytes uploaded in [{}] parts.", key, totalBytesRead,
      uploadPartNumber.get());
  }

  @Override
  public String toString() {
    return String.format("S3MultiPartUpload uploading to %s/%s, with uploadId - %s", bucketName, key, uploadId);
  }
}
