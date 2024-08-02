package com.tech.engg5.xml.batch.processor.model.bookTransactionRaw;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookTransactionPayload {

  @JacksonXmlProperty(localName = "alert_Data_Id")
  String alertDataId;

  @JacksonXmlElementWrapper(localName = "Alert_Book_Info")
  AlertBookInfo alertBookInfo;

  String transactionType;

  @JacksonXmlElementWrapper(localName = "Alert_Body_Info")
  AlertBodyInfo alertBodyInfo;
}
