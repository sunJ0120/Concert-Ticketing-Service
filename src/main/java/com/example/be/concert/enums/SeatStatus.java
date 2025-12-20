package com.example.be.concert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
  AVAILABLE("예매가능", "예매할 수 있는 좌석"),
  LOCKED("선택중", "다른 사용자가 선택 중 (5분 타임아웃)"),
  RESERVED("예약완료", "예약이 완료된 좌석"),
  SOLD("판매완료", "결제까지 완료된 좌석"),
  UNAVAILABLE("판매불가", "판매하지 않는 좌석");

  private final String displayName;
  private final String description;
}
