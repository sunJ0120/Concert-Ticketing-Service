package com.example.be.concert.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.be.concert.domain.Concert;
import com.example.be.concert.enums.ConcertStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/test-data/concert-repository-test.sql")
class ConcertRepositoryTest {

  @Autowired private ConcertRepository concertRepository;

  @Test
  void 예매오픈_상태_공연수_카운트() {
    // given - 테스트 데이터
    // when
    long count = concertRepository.countByConcertStatus(ConcertStatus.BOOKING_OPEN);
    // then
    assertEquals(1, count);
  }

  @Test
  void 공연_상세_조회_성공() {
    // given - 테스트 데이터
    // when
    Optional<Concert> concert = concertRepository.findByIdWithDetail(1L);
    // then
    assertThat(concert).isPresent();
    assertThat(concert.get().getTitle()).isEqualTo("IU Concert");
  }

  @Test
  void 존재하지_않는_공연_조회시_빈값_반환() {
    // given - 테스트 데이터
    // when
    Optional<Concert> concert = concertRepository.findByIdWithDetail(99L);
    // then
    assertThat(concert).isEmpty();
  }

  @Test
  void fetch_join으로_연관_엔티티_한번에_로딩() {
    // given - 테스트 데이터
    // when
    Concert concert = concertRepository.findByIdWithDetail(1L).orElseThrow();
    // then
    assertThat(concert.getHallTemplate().getHallName()).isEqualTo("테스트홀");
    assertThat(concert.getHallTemplate().getBuilding().getName()).isEqualTo("테스트 공연장");
    assertThat(concert.getConcertSections()).hasSize(2);
  }

  @Test
  void 섹션_없는_공연도_조회_가능() {
    // given - 테스트 데이터
    // when
    Concert concert = concertRepository.findByIdWithDetail(3L).orElseThrow();
    // then
    assertThat(concert.getHallTemplate().getHallName()).isEqualTo("테스트홀");
    assertThat(concert.getHallTemplate().getBuilding().getName()).isEqualTo("테스트 공연장");
    assertThat(concert.getConcertSections()).isEmpty();
  }
}
