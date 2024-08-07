package com.tech.engg5.xml.batch.processor.service;

import java.io.IOException;
import java.io.InputStream;

public interface DestinationFileService {
  void uploadToS3RawBucket(InputStream inputStream, String filename, String rawBucketName, long fileSize)
    throws InterruptedException, IOException;

  void unzipFileToS3ExtractBucket(String baseFilename, String rawBucketName, String extBucketName, int bufferSize)
    throws IOException;

  InputStream getFileAsInputStream(String bucketName, String filename);
}
