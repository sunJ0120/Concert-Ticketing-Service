package com.example.be.payment;

import com.example.be.common.BaseTimeEntity;
import com.example.be.reservation.Reservation;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_reservation"))
  private Reservation reservation;

  @Column(name = "order_id", nullable = false, length = 50, unique = true)
  private String orderId;

  @Column(nullable = false, precision = 10, scale = 0)
  private BigDecimal amount;

  @Column(name = "payment_method", nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false, length = 20)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  @Column(name = "pg_transaction_id", length = 100)
  private String pgTransactionId;

  @Column(name = "pg_response_code", length = 20)
  private String pgResponseCode;

  @Column(name = "initiated_at", nullable = false)
  private LocalDateTime initiatedAt;

  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  @Builder
  public Payment(Reservation reservation, String orderId, BigDecimal amount,
      PaymentMethod paymentMethod) {
    validateAmount(amount);
    this.reservation = reservation;
    this.orderId = orderId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.initiatedAt = LocalDateTime.now();

    // 0원 결제는 PG 거치지 않고 바로 완료 처리
    if (isZeroPayment()) {
      this.paymentStatus = PaymentStatus.COMPLETED;
      this.confirmedAt = LocalDateTime.now();
    }
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("결제 금액은 0 이상이어야 합니다.");
    }
  }

  private boolean isZeroPayment() {
    return amount.compareTo(BigDecimal.ZERO) == 0;
  }
}
