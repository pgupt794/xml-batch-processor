package com.tech.engg5.xml.batch.processor.exception;

public class S3MultiPartUploadException extends RuntimeException {
  public S3MultiPartUploadException() {
    super();
  }

  public S3MultiPartUploadException(String message, Throwable t) {
    super(message, t);
  }
}
