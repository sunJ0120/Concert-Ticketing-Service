package com.example.be.concert.domain;

import com.example.be.common.BaseEntity;
import com.example.be.concert.enums.SeatLabel;
import com.example.be.concert.enums.SeatStatus;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "concert_seats",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_concert_seat",
            columnNames = {"concert_id", "hall_seat_position_id"}
        )
    }
)
public class ConcertSeat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "concert_id", nullable = false, foreignKey = @ForeignKey(name = "fk_concert_seat_concert"))
  private Concert concert;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hall_seat_position_id", nullable = false, foreignKey = @ForeignKey(name = "fk_concert_seat_position"))
  private HallSeatPosition hallSeatPosition;

  @Enumerated(EnumType.STRING)
  @Column(name = "section_name", nullable = false, length = 50)
  private SeatLabel sectionLabel;

  @Enumerated(EnumType.STRING)
  @Column(name = "seat_status", length = 20, nullable = false)
  private SeatStatus seatStatus;

  @Builder
  public ConcertSeat(Concert concert, HallSeatPosition hallSeatPosition,
      SeatLabel sectionLabel, SeatStatus seatStatus) {
    this.concert = concert;
    this.hallSeatPosition = hallSeatPosition;
    this.sectionLabel = sectionLabel;
    this.seatStatus = seatStatus != null ? seatStatus : SeatStatus.AVAILABLE;
  }
}
