package com.tech.engg5.xml.batch.processor.model.bookTransactionRaw;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlertTransactionCancelled {

  @JacksonXmlProperty(localName = "transaction_Id")
  String transactionId;

  @JacksonXmlProperty(localName = "transaction_Cancel_Reason")
  String transactionCancelReason;

  @JacksonXmlProperty(localName = "transaction_Minimum_Amount")
  String transactionMinimumAmount;

  @JacksonXmlProperty(localName = "transaction_Amount")
  String transactionAmount;

  @JacksonXmlProperty(localName = "transaction_Method_Name")
  String transactionMethodName;

  @JacksonXmlProperty(localName = "transaction_Method_Last_Four_Digits")
  String transactionMethodLastFourDigits;

  @JacksonXmlProperty(localName = "transaction_Date")
  String transactionDate;

  @JacksonXmlProperty(localName = "transaction_Payee_Name")
  String transactionPayeeName;

  @JacksonXmlProperty(localName = "transaction_Payee_Address")
  String transactionPayeeAddress;

  @JacksonXmlProperty(localName = "transaction_Payee_Address_City")
  String transactionPayeeAddressCity;

  @JacksonXmlProperty(localName = "transaction_Payee_Address_State")
  String transactionPayeeAddressState;

  @JacksonXmlProperty(localName = "transaction_Payee_Address_Zip")
  String transactionPayeeAddressZip;

  @JacksonXmlProperty(localName = "transaction_Payee_Phone_Number")
  String transactionPayeePhoneNumber;
}
