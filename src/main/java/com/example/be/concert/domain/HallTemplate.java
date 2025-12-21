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
import jakarta.validation.constraints.NotBlank;
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
    name = "hall_templates",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_building_hall",
            columnNames = {"building_id", "hall_name"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class HallTemplate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "building_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hall_template_building"))
  private Building building;

  @NotBlank(message = "홀 이름은 필수입니다.")
  @Column(name = "hall_name", nullable = false, length = 50)
  private String hallName;

  @NotNull(message = "전체 좌석 수는 필수입니다.")
  @Min(value = 1, message = "전체 좌석 수는 1 이상이어야 합니다.")
  @Column(name = "total_seats", nullable = false)
  private Integer totalSeats;

  @NotNull(message = "전체 줄 수는 필수입니다.")
  @Min(value = 1, message = "전체 줄 수는 1 이상이어야 합니다.")
  @Column(name = "total_rows", nullable = false)
  private Integer totalRows;

  @Builder
  public HallTemplate(Building building, String hallName,
      Integer totalSeats, Integer totalRows) {
    this.building = building;
    this.hallName = hallName;
    this.totalSeats = totalSeats;
    this.totalRows = totalRows;
  }
}
