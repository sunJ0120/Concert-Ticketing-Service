package com.example.be.concert.repository;

import com.example.be.concert.domain.Concert;
import com.example.be.concert.dto.ConcertCursor;
import com.example.be.concert.dto.ConcertSearchCondition;
import com.example.be.concert.enums.ConcertSortType;
import java.util.List;

public interface ConcertRepositoryCustom {

  public List<Concert> findWithCursor(
      ConcertSearchCondition condition,
      ConcertCursor cursor,
      int size,
      ConcertSortType sortType,
      boolean isAsc);
}
