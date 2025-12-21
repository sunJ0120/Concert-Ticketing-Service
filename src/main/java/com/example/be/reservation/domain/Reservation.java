package com.example.be.reservation.domain;

import com.example.be.common.BaseEntity;
import com.example.be.concert.domain.ConcertSeat;
import com.example.be.reservation.enums.ReservationStatus;
import com.example.be.user.domain.User;
import jakarta.persistence.Column;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
public class Reservation extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_reservation_user"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seat_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_reservation_seat"))
  private ConcertSeat concertSeat;

  @NotNull(message = "가격은 필수입니다.")
  @Min(value = 0, message = "가격은 0 이상이어야 합니다")
  @Column(nullable = false, precision = 10, scale = 0)
  private Integer price;

  @Enumerated(EnumType.STRING)
  @Column(name = "reservation_status", nullable = false, length = 20)
  private ReservationStatus reservationStatus = ReservationStatus.PENDING;

  @NotNull(message = "예약 일시는 필수입니다.")
  @Column(name = "reserved_at", nullable = false)
  private LocalDateTime reservedAt;

  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Builder
  public Reservation(User user, ConcertSeat concertSeat, Integer price,
      LocalDateTime expiresAt) {
    validateExpiresAt(expiresAt);

    this.user = user;
    this.concertSeat = concertSeat;
    this.price = price;
    this.reservationStatus = ReservationStatus.PENDING;
    this.reservedAt = LocalDateTime.now();
    this.expiresAt = expiresAt;
  }

  private void validateExpiresAt(LocalDateTime expiresAt) {
    if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("만료 시간은 현재 시간 이후여야 합니다.");
    }
  }
}
