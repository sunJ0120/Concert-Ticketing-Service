package com.example.be.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventType {
  INITIATED("결제시작", "결제가 시작됨"),
  CONFIRMED("결제완료", "결제가 완료됨"),
  FAILED("결제실패", "결제가 실패함"),
  CANCELLED("결제취소", "결제가 취소됨"),
  REFUNDED("환불완료", "환불이 완료됨"),
  PARTIAL_REFUNDED("부분환불", "부분 환불됨");

  private final String displayName;
  private final String description;
}
