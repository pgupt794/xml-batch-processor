package com.tech.engg5.xml.batch.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
@EnableScheduling
public class XmlBatchProcessorApplication {
  public static void main(String[] args) {
    ReactorDebugAgent.init();
    System.setProperty("APP_ID", "1000153");
    System.setProperty("APP_NAME", "xml-batch-processor");
    SpringApplication application = new SpringApplication(XmlBatchProcessorApplication.class);
    application.run(args);
  }
}
