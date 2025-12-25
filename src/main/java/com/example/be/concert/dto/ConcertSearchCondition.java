package com.example.be.concert.dto;

import com.example.be.concert.enums.ConcertStatus;
import com.example.be.concert.enums.Genre;
import java.time.LocalDate;

// 검색 조건 필터링 전용 dto
public record ConcertSearchCondition(
    String keyword,
    Genre genre,
    ConcertStatus status,
    LocalDate startDate,
    LocalDate endDate,
    Integer minPrice,
    Integer maxPrice) {

  public static ConcertSearchCondition from(ConcertCursorRequest request) {
    return new ConcertSearchCondition(
        request.keyword(),
        request.genre(),
        request.status(),
        request.startDate(),
        request.endDate(),
        request.minPrice(),
        request.maxPrice());
  }
}
