package com.example.be.payment.domain;

import com.example.be.common.BaseTimeEntity;
import com.example.be.payment.enums.EventType;
import com.example.be.payment.enums.PaymentStatus;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_logs")
@EntityListeners(AuditingEntityListener.class)
public class PaymentLog extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_log_payment"))
  private Payment payment;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 20)
  private EventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(name = "old_status", length = 20)
  private PaymentStatus oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", length = 20)
  private PaymentStatus newStatus;

  @Column(name = "pg_response_code", length = 20)
  private String pgResponseCode;

  @Column(name = "pg_response_message", columnDefinition = "TEXT")
  private String pgResponseMessage;

  @Column(name = "pg_raw_response", columnDefinition = "TEXT")
  private String pgRawResponse;

  @Builder
  public PaymentLog(Payment payment, EventType eventType, PaymentStatus oldStatus,
      PaymentStatus newStatus, String pgResponseCode, String pgResponseMessage,
      String pgRawResponse) {
    this.payment = payment;
    this.eventType = eventType;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.pgResponseCode = pgResponseCode;
    this.pgResponseMessage = pgResponseMessage;
    this.pgRawResponse = pgRawResponse;
  }
}
