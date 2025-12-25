package com.example.be.concert.dto;

import com.example.be.concert.domain.Concert;
import java.time.LocalDate;

public record ConcertSummaryDto(
    Long id,
    String title,
    String artist,
    String genreDisplayName,
    String posterUrl,
    LocalDate startDate,
    LocalDate endDate,
    String venueName,
    Integer minPrice,
    String status,
    String statusDisplayName,
    Long viewCount) {

  public static ConcertSummaryDto from(Concert concert) {
    // @formatter:off
    return new ConcertSummaryDto(
        concert.getId(),
        concert.getTitle(),
        concert.getArtist(),
        concert.getGenre().getDisplayName(),
        concert.getPosterUrl(),
        concert.getStartDate().toLocalDate(),
        concert.getEndDate().toLocalDate(),
        concert.getHallTemplate().getFullVenueName(),
        concert.getMinPrice(),
        concert.getConcertStatus().name(),
        concert.getConcertStatus().getDisplayName(),
        concert.getViewCount());
  }
}
