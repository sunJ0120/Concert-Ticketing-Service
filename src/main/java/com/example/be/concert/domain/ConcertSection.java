package com.example.be.concert.domain;

import com.example.be.concert.enums.SeatLabel;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSection {

  @Enumerated(EnumType.STRING)
  @Column(name = "section_name", nullable = false, length = 50)
  private SeatLabel sectionLabel;

  @NotNull(message = "시작 행은 필수입니다.")
  @Min(value = 1, message = "시작 행은 1 이상이어야 합니다.")
  @Column(name = "row_start", nullable = false)
  private Integer rowStart;

  @NotNull(message = "종료 행은 필수입니다.")
  @Min(value = 1, message = "종료 행은 1 이상이어야 합니다.")
  @Column(name = "row_end", nullable = false)
  private Integer rowEnd;

  @NotNull(message = "가격은 필수입니다.")
  @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
  @Column(name = "price", nullable = false)
  private Integer price;

  @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "올바른 색상 코드 형식이 아닙니다")
  @Column(name = "color", length = 7)
  private String color = "#808080";    // HEX color code (#FFFFFF)

  @Builder
  public ConcertSection(SeatLabel sectionLabel, Integer rowStart, Integer rowEnd, Integer price) {
    validateRows(rowStart, rowEnd);
    this.sectionLabel = sectionLabel;
    this.rowStart = rowStart;
    this.rowEnd = rowEnd;
    this.price = price;
    this.color = color != null ? color : "#808080";
  }

  private void validateRows(Integer rowStart, Integer rowEnd) {
    if (rowStart != null && rowEnd != null && rowStart > rowEnd) {
      throw new IllegalArgumentException("시작 행은 종료 행보다 작거나 같아야 합니다.");
    }
  }
}
