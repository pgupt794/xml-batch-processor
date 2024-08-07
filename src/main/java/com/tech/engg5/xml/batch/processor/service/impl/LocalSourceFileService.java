package com.tech.engg5.xml.batch.processor.service.impl;

import com.tech.engg5.xml.batch.processor.exception.NonRetryableException;
import com.tech.engg5.xml.batch.processor.model.FileDetails;
import com.tech.engg5.xml.batch.processor.service.SourceFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(value = "batch-processor.src.local", havingValue = "true", matchIfMissing = false)
public class LocalSourceFileService implements SourceFileService {

  private final String path;

  public LocalSourceFileService(@Value("${batch-processor.src.local-path}") String path) {
    LOG.info("Created local file sourcing path - [{}]", path);
    if (StringUtils.isBlank(path)) {
      throw new IllegalStateException("Path cannot be empty.");
    }
    this.path = path;
  }

  @Override
  public List<FileDetails> sourceFile() {
    LOG.info("Sourcing the file on local-path for processing.");
    try {
      List<File> foundFiles = findZipFiles();
      List<FileDetails> fileDetailsList = new ArrayList<>();
      if (foundFiles.isEmpty()) {
        LOG.error("No file found on local-path.");
        return null;
      }
      for (File fileResult : foundFiles) {
        FileDetails fileDetails = FileDetails.builder()
          .filename(fileResult.getName())
          .size(fileResult.length())
          .build();
        fileDetailsList.add(fileDetails);
      }
      return fileDetailsList;
    } catch (IOException exc) {
      LOG.error("Error occurred during file search on local-path.");
      throw new NonRetryableException("Error occurred during file search on local-path.");
    }
  }

  @Override
  public InputStream getFileAsInputStream(String filename) {
    LOG.info("Returning local file as input-stream.");
    try {
      File file = findZipFiles()
        .stream()
        .filter(f -> f.getName().equalsIgnoreCase(filename))
        .findFirst()
        .get();
      return new FileInputStream(file);
    } catch (IOException exc) {
      LOG.error("Error occurred while returning the file as input-stream.");
      throw new NonRetryableException("Error occurred while returning the file as input-stream.");
    }
  }

  @Override
  public void disconnect() {
    LOG.info("No need to disconnect. Using local file service.");
  }

  @Override
  public void connect() {
    LOG.info("No need to connect. Using local file servicing.");
  }

  private List<File> findZipFiles() throws IOException {
    return Files.list(Path.of(path))
      .map(Path::toFile)
      .filter(File::isFile)
      .filter(zipFilePredicate())
      .collect(Collectors.toList());
  }

  private static Predicate<? super File> zipFilePredicate() {
    return (Predicate<File>) file -> Optional.of(file)
      .map(File::getName)
      .filter(name -> name.length() >= 4)
      .filter(name -> name.endsWith(".gz") || name.toLowerCase().endsWith(".gz.pgp"))
      .isPresent();
  }
}
