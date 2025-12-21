package com.example.be.concert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
  SCHEDULED("예정", "공연이 예정되어 있습니다"),
  BOOKING_OPEN("예매중", "현재 예매가 진행중입니다"),
  BOOKING_CLOSED("예매마감", "예매가 마감되었습니다"),
  SOLD_OUT("매진", "모든 좌석이 매진되었습니다"),
  IN_PROGRESS("진행중", "공연이 진행중입니다"),
  COMPLETED("종료", "공연이 종료되었습니다"),
  CANCELLED("취소", "공연이 취소되었습니다");

  private final String displayName;
  private final String description;
}
