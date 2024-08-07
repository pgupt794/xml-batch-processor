package com.tech.engg5.xml.batch.processor.controller;

import com.tech.engg5.xml.batch.processor.batchJob.BatchJobRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/book/transaction/fileProcessor")
public class BookTransactionController {

  @Autowired
  BatchJobRunner batchJobRunner;

  @GetMapping("/batch/processFile")
  public Mono<Void> processBatchFile() {
    LOG.info("Manual Processing - Triggered main job.");
    return batchJobRunner.executeBatchJob();
  }
}
