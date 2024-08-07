package com.tech.engg5.xml.batch.processor.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.tech.engg5.xml.batch.processor.model.properties.AppProperties;
import com.tech.engg5.xml.batch.processor.model.properties.RetryConfig;
import com.tech.engg5.xml.batch.processor.service.impl.S3DestinationFileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class S3DestinationFileServiceTest {

  @Mock
  TransferManager transferManager;

  @Mock
  private Upload upload;

  @InjectMocks
  @Spy
  S3DestinationFileService s3DestinationFileService;

  @Mock
  UploadResult uploadResult;

  @Mock
  private AmazonS3 s3Client;

  @Mock
  AppProperties appProperties;

  @Mock
  RetryConfig retryConfig;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    doReturn(transferManager).when(s3DestinationFileService).getTransferManager();
    doReturn(retryConfig).when(appProperties).getRetryConfig();
  }

  @Test
  @SneakyThrows
  @DisplayName("Verify that file is uploaded to S3 bucket.")
  void shouldUploadBatchFileToS3Bucket(CapturedOutput capturedOutput) {
    String fileAvail = "src/test/resources/fixtures/bookTransactions/rawRequests/";
    String filename = "book-transaction-payload.xml";

    InputStream inputStream = new FileInputStream(new File(fileAvail + filename));
    byte[] file = IOUtils.toByteArray(inputStream);

    String bucketName = "test-bucket";
    long fileSize = file.length;

    when(upload.waitForUploadResult()).thenReturn(uploadResult);
    when(transferManager.upload(any(), any(), any(), any(ObjectMetadata.class))).thenReturn(upload);
    when(uploadResult.getETag()).thenReturn("eTag123");
    when(retryConfig.getMaxRetries()).thenReturn(5);
    doNothing().when(s3DestinationFileService).createBucketIfNotExist(bucketName);

    s3DestinationFileService.uploadToS3RawBucket(inputStream, filename, bucketName, fileSize);

    verify(s3DestinationFileService).createBucketIfNotExist(bucketName);
    verify(transferManager).upload(eq(bucketName), eq(filename), eq(inputStream), any(ObjectMetadata.class));
    verify(upload).waitForUploadResult();
    verify(uploadResult).getETag();

    assertThat(capturedOutput).containsSubsequence("Uploading file - [book-transaction-payload.xml]"
      + " to rawBucket - [test-bucket]");
    assertThat(capturedOutput).containsSubsequence("File [book-transaction-payload.xml] upload "
      + "complete to bucket [test-bucket] with eTag: [eTag123]");
  }

  @Test
  @SneakyThrows
  @DisplayName("Verify that retry happened when error occurred while uploading the file.")
  void shouldRetryWhenFailureOccurWhileUploading(CapturedOutput capturedOutput) {
    String fileAvail = "src/test/resources/fixtures/bookTransactions/rawRequests/";
    String filename = "book-transaction-payload.xml";

    InputStream inputStream = new FileInputStream(new File(fileAvail + filename));
    byte[] file = IOUtils.toByteArray(inputStream);

    String bucketName = "test-bucket";
    long fileSize = file.length;

    doNothing().when(s3DestinationFileService).createBucketIfNotExist(bucketName);
    when(retryConfig.getMaxRetries()).thenReturn(5);
    when(retryConfig.getDelay()).thenReturn(5000L);

    when(transferManager.upload(any(), any(), any(), any(ObjectMetadata.class)))
      .thenThrow(new RuntimeException("Upload failure"))
      .thenReturn(mock(Upload.class));

    s3DestinationFileService.uploadToS3RawBucket(inputStream, filename, bucketName, fileSize);

    verify(transferManager, times(5)).upload(any(), any(), any(), any(ObjectMetadata.class));

    assertThat(capturedOutput).containsSubsequence("Uploading file - [book-transaction-payload.xml]"
      + " to rawBucket - [test-bucket]");

    assertThat(capturedOutput).containsSubsequence("Attempt 1 failed for uploading file "
      + "[book-transaction-payload.xml], retrying");
    assertThat(capturedOutput).containsSubsequence("Attempt 2 failed for uploading file "
      + "[book-transaction-payload.xml], retrying");
    assertThat(capturedOutput).containsSubsequence("Attempt 3 failed for uploading file "
      + "[book-transaction-payload.xml], retrying");
    assertThat(capturedOutput).containsSubsequence("Attempt 4 failed for uploading file "
      + "[book-transaction-payload.xml], retrying");
    assertThat(capturedOutput).containsSubsequence("Upload failed after 5 attempts. All Retries exhausted for "
      + "file [book-transaction-payload.xml]");
  }
}
