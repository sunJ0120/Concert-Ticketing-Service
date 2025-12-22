package com.example.be.concert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Genre {
  KPOP("K-POP", "K-POP 아이돌 콘서트"),
  CONCERT("콘서트", "국내외 아티스트 공연"),
  MUSICAL("뮤지컬", "창작/라이선스 뮤지컬"),
  THEATER("연극", "연극/드라마"),
  CLASSIC("클래식", "클래식/오페라/발레"),
  SPORTS("스포츠", "스포츠 경기"),
  FESTIVAL("페스티벌", "음악 페스티벌"),
  EXHIBITION("전시", "전시/행사");

  private final String displayName;
  private final String description;
}
