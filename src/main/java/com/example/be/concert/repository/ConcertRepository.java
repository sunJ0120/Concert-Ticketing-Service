package com.example.be.concert.repository;

import com.example.be.concert.domain.Concert;
import com.example.be.concert.enums.ConcertStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertRepositoryCustom {

  long countByConcertStatus(ConcertStatus status); // 상태별 카운트

  List<Concert> findTop10ByConcertStatusInOrderByViewCountDesc(
      Collection<ConcertStatus> concertStatuses);

  @Query(
      "SELECT c FROM Concert c "
          + "JOIN FETCH c.hallTemplate ht "
          + "JOIN FETCH ht.building "
          + "LEFT JOIN FETCH c.concertSections "
          + "WHERE c.id = :id")
  Optional<Concert> findByIdWithDetail(@Param("id") Long id);
}
