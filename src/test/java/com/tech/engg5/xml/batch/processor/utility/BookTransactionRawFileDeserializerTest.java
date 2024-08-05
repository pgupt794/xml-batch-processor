package com.tech.engg5.xml.batch.processor.utility;

import com.tech.engg5.xml.batch.processor.Fixture;
import com.tech.engg5.xml.batch.processor.model.bookTransactionRaw.BookTransactionPayload;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BookTransactionRawFileDeserializerTest {

  @Autowired
  BookTransactionRawFileDeserializer bookTransactionRawFileDeserializer;

  @Test
  @DisplayName("Verify that xml payload should be deserialized to json payload successfully")
  void acceptShouldFlushDeserializedBookTransactionRequestOnSuccessfulValidationAndDeserialization() {
    String incomingRawRequest = Fixture.RAW_REQUESTS.loadFixture("book-transaction-payload.xml");

    BookTransactionPayload bookTransactionPayload = Flux.just(incomingRawRequest)
      .handle(bookTransactionRawFileDeserializer).blockFirst();

    assert bookTransactionPayload != null;
    Assertions.assertEquals("1111", bookTransactionPayload.getAlertDataId());
    assert bookTransactionPayload.getAlertBookInfo().getBookPublisher().size() == 2;
  }

  @Test
  @DisplayName("Verify that error is thrown when deserializing the xml request")
  void acceptShouldSignalErrorWhenErrorOccurs() {
    String incomingRawRequest = Fixture.RAW_REQUESTS.loadFixture("invalid-book-transaction-payload.xml");
    val verifier = StepVerifier.create(Flux.just(incomingRawRequest)
      .handle(bookTransactionRawFileDeserializer)).expectSubscription();

    verifier.expectErrorSatisfies(error -> assertThat(error)
      .isInstanceOf(Exception.class)
      .hasMessageStartingWith("Invalid alert xml tag."))
      .verify();
  }
}
