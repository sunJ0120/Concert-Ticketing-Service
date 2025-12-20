package com.example.be.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentMethod {
  CARD("카드결제"),
  ACCOUNT("계좌이체"),
  VIRTUAL_ACCOUNT("가상계좌"),
  MOBILE("휴대폰결제");

  private final String displayName;
}
