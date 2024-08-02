package com.tech.engg5.xml.batch.processor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.io.IOUtils.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Fixture {

  RAW_REQUESTS("bookTransactions/rawRequests"),
  RAW_TRANSFORMED_REQUESTS("bookTransactions/rawTransformedRequests");

  String path;

  @SneakyThrows
  public String loadFixture(String filename, SubPath... subPaths) {
    String fixturePath = "fixtures/" + this.path + '/' + join(subPaths, '/') + '/' + filename;
    try (InputStream inputStream = new ClassPathResource(fixturePath).getInputStream()) {
      return new String(toByteArray(inputStream), UTF_8);
    }
  }

  @SneakyThrows
  public String loadFixture(String filename) {
    String fixturePath = "fixtures/" + this.path + '/' + filename;
    try (InputStream inputStream = new ClassPathResource(fixturePath).getInputStream()) {
      return new String(toByteArray(inputStream), UTF_8);
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  public enum SubPath {

    CLOUD_EVENT("cloud-event");

    String subPath;

    @Override
    public String toString() {
      return subPath;
    }
  }
}
