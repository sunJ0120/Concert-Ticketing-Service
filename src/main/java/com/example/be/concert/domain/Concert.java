package com.example.be.concert.domain;

import com.example.be.common.BaseEntity;
import com.example.be.concert.enums.ConcertStatus;
import com.example.be.concert.enums.Genre;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concerts")
@EntityListeners(AuditingEntityListener.class)
public class Concert extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "hall_template_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_concert_hall_template"))
  private HallTemplate hallTemplate;

  @NotBlank(message = "타이틀 정보는 필수입니다.")
  @Column(nullable = false, length = 300)
  private String title;

  @Column(length = 200)
  private String artist;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "poster_url", length = 500)
  private String posterUrl;

  @NotNull(message = "공연 시작 일시는 필수입니다.")
  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @NotNull(message = "공연 종료 일시는 필수입니다.")
  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @NotNull(message = "예약 시작 일시는 필수입니다.")
  @Column(name = "booking_start_at", nullable = false)
  private LocalDateTime bookingStartAt;

  @NotNull(message = "예약 종료 일시는 필수입니다.")
  @Column(name = "booking_end_at", nullable = false)
  private LocalDateTime bookingEndAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "concert_status", length = 20, nullable = false)
  private ConcertStatus concertStatus;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private Genre genre;

  @Column(name = "view_count")
  private long viewCount;

  @Column(name = "min_price")
  private Integer minPrice;

  @Column(name = "max_price")
  private Integer maxPrice;

  @ElementCollection
  @CollectionTable(
      name = "concert_sections",
      joinColumns = @JoinColumn(name = "concert_id"),
      foreignKey = @ForeignKey(name = "fk_concert_section_concert"),
      uniqueConstraints =
          @UniqueConstraint(
              name = "uk_concert_section",
              columnNames = {"concert_id", "section_name"}))
  private List<ConcertSection> concertSections = new ArrayList<>();

  @Builder
  public Concert(
      HallTemplate hallTemplate,
      String title,
      String artist,
      String description,
      String posterUrl,
      LocalDateTime startDate,
      LocalDateTime endDate,
      LocalDateTime bookingStartAt,
      LocalDateTime bookingEndAt,
      ConcertStatus concertStatus,
      Genre genre,
      Integer minPrice,
      Integer maxPrice,
      long viewCount,
      List<ConcertSection> concertSections) {
    validateConcertPrice(minPrice, maxPrice);
    validateConcertDate(startDate, endDate);
    validateBookingDate(bookingStartAt, bookingEndAt);

    this.hallTemplate = hallTemplate;
    this.title = title;
    this.artist = artist;
    this.description = description;
    this.posterUrl = posterUrl;
    this.startDate = startDate;
    this.endDate = endDate;
    this.bookingStartAt = bookingStartAt;
    this.bookingEndAt = bookingEndAt;
    this.concertStatus = concertStatus != null ? concertStatus : ConcertStatus.SCHEDULED;
    this.genre = genre;
    this.viewCount = concertStatus != null ? viewCount : 0;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;

    if (concertSections != null) {
      this.concertSections = concertSections;
    }
  }

  public int getMinPrice() {
    return concertSections.stream().mapToInt(ConcertSection::getPrice).min().orElse(0);
  }

  // 섹션 변경 시 갱신
  public void updatePriceRange() {
    this.minPrice = concertSections.stream().mapToInt(ConcertSection::getPrice).min().orElse(0);
    this.maxPrice = concertSections.stream().mapToInt(ConcertSection::getPrice).max().orElse(0);
  }

  public void incrementViewCount() {
    this.viewCount += 1;
  }

  private void validateConcertPrice(Integer minPrice, Integer maxPrice) {
    if (minPrice == null || maxPrice == null) {
      return;
    }
    if (minPrice > maxPrice) {
      throw new IllegalArgumentException("최소 금액은 최대 금액보다 작아야 합니다.");
    }
  }

  private void validateConcertDate(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null || endDate == null) {
      return;
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("시작 일자는 종료 일자보다 작아야 합니다.");
    }
  }

  private void validateBookingDate(LocalDateTime bookingStartAt, LocalDateTime bookingEndAt) {
    if (bookingStartAt == null || bookingEndAt == null) {
      return;
    }
    if (bookingStartAt.isAfter(bookingEndAt)) {
      throw new IllegalArgumentException("예약 시작 일자는 예약 종료 일자보다 작아야 합니다.");
    }
  }
}
