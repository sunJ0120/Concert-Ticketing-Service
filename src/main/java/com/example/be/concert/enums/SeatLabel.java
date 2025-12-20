package com.example.be.concert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatLabel {
  VIP("VIP석", "최고급 좌석"),
  R("R석", "1등급 좌석"),
  S("S석", "2등급 좌석"),
  A("A석", "3등급 좌석"),
  STANDING("스탠딩", "입석"),
  WHEELCHAIR("휠체어석", "장애인 전용석"),
  COMPANION("동반석", "장애인 동반 좌석");

  private final String displayName;
  private final String description;
}
