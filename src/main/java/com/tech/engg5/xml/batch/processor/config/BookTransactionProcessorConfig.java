package com.tech.engg5.xml.batch.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

@Configuration
public class BookTransactionProcessorConfig {

  @Bean
  public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter() {
    return new MappingJackson2XmlHttpMessageConverter();
  }
}
