package com.example.be.concert.dto;

import com.example.be.concert.enums.ConcertStatus;
import com.example.be.concert.enums.Genre;
import java.time.LocalDate;

public record ConcertCursorRequest(
    String keyword,
    Genre genre,
    ConcertStatus status,
    LocalDate startDate,
    LocalDate endDate,
    Integer minPrice,
    Integer maxPrice,
    String cursor,
    Integer size,
    String sort) {

  public int getValidSize() {
    return (size != null && size > 0 && size <= 100) ? size : 20;
  }
}
