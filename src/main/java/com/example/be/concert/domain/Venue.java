package com.example.be.concert;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Venue {

  @Column(name = "venue_name", nullable = false, length = 100)
  private String name;

  @Column(name = "venue_address", nullable = false, length = 300)
  private String address;

  @Column(name = "venue_seats")
  private Integer totalSeats;

  @Column(name = "venue_latitude")
  private Double latitude;

  @Column(name = "venue_longitude")
  private Double longitude;

  public static Venue of(String name, String address, int totalSeats,
      double latitude, double longitude) {
    return Venue.builder()
        .name(name)
        .address(address)
        .totalSeats(totalSeats)
        .latitude(latitude)
        .longitude(longitude)
        .build();
  }
}
