package com.example.be.concert;

import com.example.be.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concerts")
@EntityListeners(AuditingEntityListener.class)
public class Concert extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 300)
  private String title;

  @Column(length = 200)
  private String artist;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "poster_url", length = 500)
  private String posterUrl;

  @Embedded
  private Venue venue;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Column(name = "booking_start_at", nullable = false)
  private LocalDateTime bookingStartAt;

  @Column(name = "booking_end_at", nullable = false)
  private LocalDateTime bookingEndAt;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Status status = Status.SCHEDULED;

  @Builder
  public Concert(String title, String artist, String description, String posterUrl, Venue venue,
      LocalDateTime startDate, LocalDateTime endDate, LocalDateTime bookingStartAt,
      LocalDateTime bookingEndAt, Status status) {
    validateVenue(venue);
    validateConcertDate(startDate, endDate);
    validateBookingDate(bookingStartAt, bookingEndAt);
    this.title = title;
    this.artist = artist;
    this.description = description;
    this.posterUrl = posterUrl;
    this.venue = venue;
    this.startDate = startDate;
    this.endDate = endDate;
    this.bookingStartAt = bookingStartAt;
    this.bookingEndAt = bookingEndAt;
    this.status = status != null ? status : Status.SCHEDULED;
  }

  private void validateVenue(Venue venue) {
    if (venue == null) {
      throw new IllegalArgumentException("공연장 정보는 필수입니다");
    }
  }

  private void validateConcertDate(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("시작 일자는 종료 일자보다 작아야 합니다.");
    }
  }

  private void validateBookingDate(LocalDateTime bookingStartAt, LocalDateTime bookingEndAt) {
    if (bookingStartAt.isAfter(bookingEndAt)) {
      throw new IllegalArgumentException("예약 시작 일자는 예약 종료 일자보다 작아야 합니다.");
    }
  }
}
