package com.tech.engg5.xml.batch.processor.model.bookTransactionRaw;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlertBookInfo {

  @JacksonXmlProperty(localName = "book_Id")
  String bookId;

  @JacksonXmlProperty(localName = "book_Name")
  String bookName;

  @JacksonXmlElementWrapper(localName = "book_Genre")
  BookGenre bookGenre;

  @JacksonXmlElementWrapper(localName = "book_Publisher")
  BookPublisher bookPublisher;

  @JacksonXmlProperty(localName = "book_Publishing_Date")
  String bookPublishingDate;

  @JacksonXmlProperty(localName = "book_Author")
  String bookAuthor;
}
