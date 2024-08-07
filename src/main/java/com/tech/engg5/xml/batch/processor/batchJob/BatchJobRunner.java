package com.tech.engg5.xml.batch.processor.batchJob;

import com.tech.engg5.xml.batch.processor.model.FileDetails;
import com.tech.engg5.xml.batch.processor.model.properties.AppProperties;
import com.tech.engg5.xml.batch.processor.service.DestinationFileService;
import com.tech.engg5.xml.batch.processor.service.SourceFileService;
import com.tech.engg5.xml.batch.processor.utility.FileUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRunner {

  private final AppProperties appProperties;
  private final DestinationFileService destinationFileService;
  private final SourceFileService sourceFileService;

  public Mono<Void> executeBatchJob() {
    LOG.info("Initiating batch-job for file-processing.");
    sourceFileService.connect();
    List<FileDetails> fileDetailsList = sourceFileService.sourceFile();
    return Mono.justOrEmpty(fileDetailsList)
      .filter(CollectionUtils::isNotEmpty)
      .flatMapMany(Flux::fromIterable)
      .concatMap(fileDetails -> {
        String fileCorrelationId = FileUtility.getFileCorrelationId(fileDetails.getFilename());

        return this.uploadFileToS3Bucket(fileDetails, fileCorrelationId);
      })
      .collectList()
      .doOnSuccess(unused -> sourceFileService.disconnect())
      .doOnError(unused -> sourceFileService.disconnect())
      .then();
  }

  public Mono<Boolean> uploadFileToS3Bucket(FileDetails fileDetails, String fileCorrelationId) {
    LOG.info("Step 1 - Transferring/Uploading the file - [{}] to S3 raw bucket.", fileDetails.getFilename());

    try (InputStream inputStream = sourceFileService.getFileAsInputStream(fileDetails.getFilename())) {
      destinationFileService.uploadToS3RawBucket(inputStream, fileDetails.getFilename(),
        appProperties.getS3().getTransactionRawBucket(), fileDetails.getSize());
      LOG.info("File - [{}] uploading completed.", fileDetails.getFilename());
      return Mono.just(Boolean.TRUE);

    } catch (Exception exc) {
      LOG.error("Error occurred while uploading the file - [{}].", fileDetails.getFilename());
      return Mono.just(Boolean.FALSE);
    }
  }
}
