package com.tech.engg5.xml.batch.processor.service;

import com.tech.engg5.xml.batch.processor.model.FileDetails;

import java.io.InputStream;
import java.util.List;

public interface SourceFileService {
  List<FileDetails> sourceFile();
  InputStream getFileAsInputStream(String filename);
  void disconnect();
  void connect();
}
