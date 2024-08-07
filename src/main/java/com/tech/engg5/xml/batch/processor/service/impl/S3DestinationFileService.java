package com.tech.engg5.xml.batch.processor.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.tech.engg5.xml.batch.processor.model.properties.AppProperties;
import com.tech.engg5.xml.batch.processor.service.DestinationFileService;
import com.tech.engg5.xml.batch.processor.utility.S3MultiPartUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "batch-processor.dest.target", havingValue = "S3", matchIfMissing = true)
public class S3DestinationFileService implements DestinationFileService {

  private final AmazonS3 s3client;
  private TransferManager transferManager;
  @Autowired
  AppProperties appProperties;

  @Override
  public void uploadToS3RawBucket(InputStream inputStream, String filename, String rawBucketName, long fileSize)
    throws InterruptedException, IOException {

    this.createBucketIfNotExist(rawBucketName);
    LOG.info("Uploading file - [{}] to rawBucket - [{}]", filename, rawBucketName);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setHeader("Content-Length", fileSize);

    int attempt = 0;
    long delay = appProperties.getRetryConfig().getDelay();
    boolean success = false;

    while (!success && attempt < appProperties.getRetryConfig().getMaxRetries()) {
      attempt++;
      try {
        Upload upload = this.getTransferManager().upload(rawBucketName, filename, inputStream, metadata);
        UploadResult result = upload.waitForUploadResult();
        LOG.info("File [{}] upload complete to bucket [{}] with eTag: [{}]", filename, rawBucketName, result.getETag());
        success = true;
      } catch (Exception exc) {
        if (attempt < appProperties.getRetryConfig().getMaxRetries()) {
          LOG.warn("Attempt {} failed for uploading file [{}], retrying.", attempt, filename);
          Thread.sleep(delay);
          delay = delay * 2;
        } else {
          LOG.error("Upload failed after {} attempts. All Retries exhausted for file [{}].", attempt, filename);
        }
      }
    }
  }

  @Override
  public void unzipFileToS3ExtractBucket(String baseFilename, String rawBucketName, String extBucketName,
    int bufferSize) throws IOException {

    this.createBucketIfNotExist(extBucketName);
    LOG.info("Unzipping file [{}] from S3. Source Bucket - [{}], Destination Bucket - [{}]", baseFilename,
      rawBucketName, extBucketName);

    S3Object s3Object = s3client.getObject(rawBucketName, baseFilename + ".gz");
    InputStream inputStream = s3Object.getObjectContent();
    GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
    LOG.info("GZIP uncompressed - [{}]", gzipInputStream);

    S3MultiPartUpload s3MultiPartUpload = new S3MultiPartUpload(extBucketName, baseFilename, s3client,
      S3MultiPartUpload.Config.DEFAULT);
    s3MultiPartUpload.uploadDataStream(gzipInputStream, bufferSize);
  }

    @Override
  public InputStream getFileAsInputStream(String bucketName, String filename) {
    S3Object s3Object = s3client.getObject(bucketName, filename);
    return s3Object.getObjectContent();
  }

  public void createBucketIfNotExist(String bucketName) {
    if (s3client.doesBucketExistV2(bucketName)) {
      LOG.info("[{}] bucket already exist.", bucketName);
      return;
    }
    LOG.info("[{}] bucket does not exist, creating new bucket.", bucketName);
    s3client.createBucket(bucketName);
  }

  public TransferManager getTransferManager() {
    if (transferManager == null) {
      transferManager = TransferManagerBuilder.standard().withS3Client(s3client).build();
    }
    return transferManager;
  }
}
