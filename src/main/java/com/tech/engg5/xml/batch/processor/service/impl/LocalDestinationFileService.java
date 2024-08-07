package com.tech.engg5.xml.batch.processor.service.impl;

import com.tech.engg5.xml.batch.processor.service.DestinationFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "batch-processor.dest.target", havingValue = "LOCAL")
public class LocalDestinationFileService implements DestinationFileService {

  @Value("${batch-processor.dest.local-path}")
  String basePath;

  @Override
  public void uploadToS3RawBucket(InputStream inputStream, String filename, String rawBucketName, long fileSize)
    throws InterruptedException, IOException {

    byte[] bytes = inputStream.readAllBytes();
    FileOutputStream outputStream = new FileOutputStream(basePath + "/" + rawBucketName + "/" + filename);
    outputStream.write(bytes);
    outputStream.close();
  }

  @Override
  public void unzipFileToS3ExtractBucket(String baseFilename, String rawBucketName, String extBucketName,
    int bufferSize) throws IOException {

    FileInputStream inputStream = new FileInputStream(basePath + "/" + rawBucketName + "/" + baseFilename + ".gz");
    FileOutputStream outputStream = new FileOutputStream(basePath + "/" + extBucketName + "/" + baseFilename);
    outputStream.write(new GZIPInputStream(inputStream).readAllBytes());
    outputStream.close();
    return;
  }

  @Override
  public InputStream getFileAsInputStream(String bucketName, String filename) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(basePath + "/" + bucketName + "/" + filename);
    } catch (FileNotFoundException exc) {
      exc.printStackTrace();
    }
    return inputStream;
  }
}
