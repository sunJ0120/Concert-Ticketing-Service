package com.example.be.concert.domain;

import com.example.be.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "buildings")
@EntityListeners(AuditingEntityListener.class)
public class Building extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "건물 이름 정보는 필수입니다.")
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "건물 주소 정보는 필수입니다.")
  @Column(nullable = false, length = 300)
  private String address;

  @NotNull(message = "건물 위도 정보는 필수입니다.")
  @Column(nullable = false)
  private Double latitude;

  @NotNull(message = "건물 경도 정보는 필수입니다.")
  @Column(nullable = false)
  private Double longitude;

  @Builder
  public Building(String name, String address, Double latitude, Double longitude) {
    this.name = name;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
