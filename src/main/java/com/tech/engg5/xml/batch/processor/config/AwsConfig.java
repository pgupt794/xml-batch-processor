package com.tech.engg5.xml.batch.processor.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.tech.engg5.xml.batch.processor.model.properties.AppProperties;
import com.tech.engg5.xml.batch.processor.model.properties.S3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AwsConfig {

  @Autowired
  AppProperties appProperties;

  @Bean
  public AmazonS3 s3Client() throws Exception {
    LOG.info("Initializing S3 client.");
    S3 s3Props = appProperties.getS3();
    System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, s3Props.isDisableCertCheck() + "");


    AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
      s3Props.getAccessKey(), s3Props.getSecretKey()));
    AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
      s3Props.getEndpoint(), s3Props.getRegion());

    ClientConfiguration clientConfiguration = new ClientConfiguration()
      .withSocketTimeout(s3Props.getSocketTimeout())
      .withConnectionTimeout(s3Props.getConnectionTimeout());

    return AmazonS3ClientBuilder.standard()
      .withCredentials(credentialsProvider)
      .withPathStyleAccessEnabled(true)
      .withEndpointConfiguration(endpointConfiguration)
      .withClientConfiguration(clientConfiguration)
      .build();
  }
}
