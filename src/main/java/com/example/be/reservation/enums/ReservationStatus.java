package com.example.be.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationStatus {
  PENDING("예약대기", "결제 대기 중"),
  CONFIRMED("예약확정", "결제 완료"),
  CANCELLED("예약취소", "사용자 취소"),
  EXPIRED("예약만료", "시간 초과로 자동 취소"),
  REFUNDED("환불완료", "환불 처리 완료");

  private final String displayName;
  private final String description;
}
