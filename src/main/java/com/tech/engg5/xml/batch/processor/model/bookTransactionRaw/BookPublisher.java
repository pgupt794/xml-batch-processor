package com.tech.engg5.xml.batch.processor.model.bookTransactionRaw;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookPublisher {

  @JacksonXmlProperty(localName = "publisher")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<String> publisher;
}
