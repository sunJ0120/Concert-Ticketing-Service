package com.example.be.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatus {
  PENDING("결제대기", "결제 진행 중"),
  COMPLETED("결제완료", "결제 성공"),
  FAILED("결제실패", "결제 실패"),
  CANCELLED("결제취소", "사용자 취소"),
  REFUNDED("환불완료", "환불 처리 완료");

  private final String displayName;
  private final String description;
}
