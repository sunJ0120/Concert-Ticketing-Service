package com.example.be.concert;

import com.example.be.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "section_templates",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_venue_section",
            columnNames = {"venue_name", "section_name"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class SectionTemplate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "venue_name", nullable = false)
  private String venueName;

  @Column(name = "section_name", nullable = false)
  private String sectionName;

  @Column(name = "row_count", nullable = false)
  private int rowCount = 0;

  @Column(name = "seats_per_row", nullable = false)
  private int seatsPerRow = 0;

  @Column(name = "color", length = 7)    // VARCHAR(7) → length 명시
  private String color;    // HEX color code (#FFFFFF)
}
