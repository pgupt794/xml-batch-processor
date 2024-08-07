package com.tech.engg5.xml.batch.processor.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtility {

  public static String getFileCorrelationId(String filename) {
    int startIndex = filename.lastIndexOf('.');
    int endIndex = filename.indexOf('-');
    return filename.substring(startIndex + 1, endIndex + 7);
  }
}
