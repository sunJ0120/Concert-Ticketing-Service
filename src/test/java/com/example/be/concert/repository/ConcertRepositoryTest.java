package com.example.be.concert.repository;

import com.example.be.concert.enums.ConcertStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/test-data/concert-repository-test.sql")
class ConcertRepositoryTest {

  @Autowired
  private ConcertRepository concertRepository;

  @Test
  void 예매오픈_상태_공연수_카운트() {
    // given - 테스트 데이터
    // when
    long count = concertRepository.countByConcertStatus(ConcertStatus.BOOKING_OPEN);
    // then
    Assertions.assertEquals(1, count);
  }
}