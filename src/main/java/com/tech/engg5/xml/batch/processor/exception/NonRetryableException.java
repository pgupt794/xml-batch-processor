package com.tech.engg5.xml.batch.processor.exception;

public class NonRetryableException extends RuntimeException {
  public NonRetryableException(String message) {
    super(message);
  }

  public NonRetryableException(String message, Throwable t) {
    super(message, t);
  }
}
