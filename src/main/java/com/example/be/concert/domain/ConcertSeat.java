package com.example.be.seat;

import com.example.be.common.BaseEntity;
import com.example.be.concert.ConcertSection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "concert_seats",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seat_position",
            columnNames = {"section_id", "row_num", "seat_num"}
        )
    }
)
public class ConcertSeat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "section_id", nullable = false, foreignKey = @ForeignKey(name = "fk_concert_seat_section"))
  private ConcertSection concertSection;

  @Column(name = "row_num", nullable = false)
  private Integer rowNum;

  @Column(name = "seat_num", nullable = false)
  private Integer seatNum;

  @Enumerated(EnumType.STRING)
  @Column(name = "seat_label", length = 20)
  private SeatLabel seatLabel;

  @Enumerated(EnumType.STRING)
  @Column(name = "seat_status", length = 20, nullable = false)
  private SeatStatus seatStatus = SeatStatus.AVAILABLE;

  @Builder
  public ConcertSeat(ConcertSection concertSection, Integer rowNum, Integer seatNum,
      SeatLabel seatLabel, SeatStatus seatStatus) {
    validatePosition(rowNum, seatNum);
    this.concertSection = concertSection;
    this.rowNum = rowNum;
    this.seatNum = seatNum;
    this.seatLabel = seatLabel;
    this.seatStatus = seatStatus != null ? seatStatus : SeatStatus.AVAILABLE;
  }

  private void validatePosition(Integer rowNum, Integer seatNum) {
    if (rowNum == null || rowNum <= 0) {
      throw new IllegalArgumentException("행 번호는 1 이상이어야 합니다");
    }
    if (seatNum == null || seatNum <= 0) {
      throw new IllegalArgumentException("좌석 번호는 1 이상이어야 합니다");
    }
  }
}
