package com.tech.engg5.xml.batch.processor.model.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties("batch-processor")
public class AppProperties {
  S3 s3;
  Destination dest;
  Source src;
  RetryConfig retryConfig;
}
