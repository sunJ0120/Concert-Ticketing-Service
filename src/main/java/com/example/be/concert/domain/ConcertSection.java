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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "concert_sections",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_concert_template",
            columnNames = {"concert_id", "template_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ConcertSection extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "concert_id", nullable = false, foreignKey = @ForeignKey(name = "fk_concert_section_concert"))
  private Concert concert;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_concert_section_template"))
  private SectionTemplate sectionTemplate;

  @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
  @Column(nullable = false, precision = 10, scale = 0)
  private BigDecimal price;

  @Column(name = "is_available", nullable = false)
  private boolean isAvailable = true;

  @Builder
  public ConcertSection(Concert concert, SectionTemplate sectionTemplate, BigDecimal price,
      Boolean isAvailable) {
    this.concert = concert;
    this.sectionTemplate = sectionTemplate;
    this.price = price;
    this.isAvailable = isAvailable != null ? isAvailable : true;
  }
}
