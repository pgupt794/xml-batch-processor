package com.tech.engg5.xml.batch.processor.utility;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.tech.engg5.xml.batch.processor.enums.BookTransactionType;
import com.tech.engg5.xml.batch.processor.exception.AlertParseException;
import com.tech.engg5.xml.batch.processor.model.bookTransactionRaw.BookTransactionPayload;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookTransactionRawFileDeserializer
  implements BiConsumer<String, SynchronousSink<BookTransactionPayload>> {

  MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter;

  @Override
  public void accept(String xmlString, SynchronousSink<BookTransactionPayload> sink) {
    try {
      XmlMapper xmlMapper = (XmlMapper) mappingJackson2XmlHttpMessageConverter.getObjectMapper();
      BookTransactionPayload bookTransactionPayload = xmlMapper.readValue(xmlString, BookTransactionPayload.class);
      bookTransactionPayload.setTransactionType(this.checkAlertTransactionType(xmlString));
      sink.next(bookTransactionPayload);
    } catch (Exception exc) {
      sink.error(new AlertParseException("Invalid alert xml tag.", exc));
    }
  }

  @SneakyThrows
  private String checkAlertTransactionType(String xmlString) {
    if (xmlString.contains("<Alert_Transaction_Cancelled>")) {
      return BookTransactionType.TRANSACTION_PAYMENT_CANCELLED.name();
    } else {
      throw new Exception("Invalid alert xml tag.");
    }
  }
}
