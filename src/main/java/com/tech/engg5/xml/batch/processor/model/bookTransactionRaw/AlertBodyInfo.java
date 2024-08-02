package com.tech.engg5.xml.batch.processor.model.bookTransactionRaw;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlertBodyInfo {

  @JacksonXmlElementWrapper(localName = "Alert_Transaction_Cancelled")
  AlertTransactionCancelled alertTransactionCancelled;
}
