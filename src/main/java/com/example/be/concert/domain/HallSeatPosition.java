package com.example.be.concert.domain;

import com.example.be.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "hall_seat_positions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hall_position",
            columnNames = {"hall_template_id", "row_num", "seat_num"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class HallSeatPosition extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hall_template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hall_seat_position_hall"))
  private HallTemplate hallTemplate;

  @NotNull(message = "행 번호는 필수입니다.")
  @Min(value = 1, message = "행 번호는 1 이상이어야 합니다.")
  @Column(name = "row_num", nullable = false)
  private Integer rowNum;

  @NotNull(message = "좌석 번호는 필수입니다.")
  @Min(value = 1, message = "좌석 번호는 1 이상이어야 합니다.")
  @Column(name = "seat_num", nullable = false)
  private Integer seatNum;

  @NotNull(message = "X 좌표는 필수입니다.")
  @Column(name = "x_coord", nullable = false)
  private Double xCoord;

  @NotNull(message = "Y 좌표는 필수입니다.")
  @Column(name = "y_coord", nullable = false)
  private Double yCoord;

  @Builder
  public HallSeatPosition(HallTemplate hallTemplate, Integer rowNum, Integer seatNum,
      Double xCoord, Double yCoord) {
    this.hallTemplate = hallTemplate;
    this.rowNum = rowNum;
    this.seatNum = seatNum;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
  }
}
