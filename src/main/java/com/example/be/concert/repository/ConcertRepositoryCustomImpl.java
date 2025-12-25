package com.example.be.concert.repository;

import static com.example.be.concert.domain.QBuilding.building;
import static com.example.be.concert.domain.QConcert.concert;
import static com.example.be.concert.domain.QConcertSection.concertSection;
import static com.example.be.concert.domain.QHallTemplate.hallTemplate;

import com.example.be.concert.domain.Concert;
import com.example.be.concert.dto.ConcertCursor;
import com.example.be.concert.dto.ConcertSearchCondition;
import com.example.be.concert.enums.ConcertSortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryCustomImpl implements ConcertRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Concert> findWithCursor(
      ConcertSearchCondition condition,
      ConcertCursor cursor,
      int size,
      ConcertSortType sortType,
      boolean isAsc) { // 파라미터 명확하게
    // @formatter:off
    return queryFactory
        .selectFrom(concert)
        .join(concert.hallTemplate, hallTemplate)
        .fetchJoin()
        .join(hallTemplate.building, building)
        .fetchJoin()
        .leftJoin(concert.concertSections, concertSection)
        .fetchJoin()
        .where(buildSearchCondition(condition), buildCursorCondition(cursor, isAsc))
        .orderBy(buildOrderSpecifier(sortType, isAsc))
        .limit(size + 1)
        .fetch();
  }

  // 검색 조건을 담기 위한
  private BooleanBuilder buildSearchCondition(ConcertSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    if (condition.keyword() != null && !condition.keyword().isEmpty()) {
      builder.and(
          concert
              .title
              .containsIgnoreCase(condition.keyword())
              .or(concert.artist.containsIgnoreCase(condition.keyword())));
    }

    if (condition.genre() != null) {
      builder.and(concert.genre.eq(condition.genre()));
    }

    if (condition.status() != null) {
      builder.and(concert.concertStatus.eq(condition.status()));
    }

    builder.and(priceBetween(condition.minPrice(), condition.maxPrice()));

    builder.and(dateBetween(condition.startDate(), condition.endDate()));

    return builder;
  }

  // 커서 조건을 담기 위한
  private BooleanExpression buildCursorCondition(ConcertCursor cursor, boolean isAsc) {
    if (cursor == null) {
      return null;
    }

    return switch (cursor.sortType()) {
      case VIEW_COUNT -> buildViewCountCondition(cursor, isAsc);
      case PRICE -> buildPriceCondition(cursor, isAsc);
      case TICKET_OPEN_AT -> buildTicketOpenAtCondition(cursor, isAsc);
      case CONCERT_DATE -> buildConcertDateCondition(cursor, isAsc);
    };
  }

  // 정렬 조건을 담기 위한 (커서 방향과 항상 일치)
  private OrderSpecifier<?>[] buildOrderSpecifier(ConcertSortType sortType, boolean isAsc) {
    return switch (sortType) {
      case VIEW_COUNT ->
          isAsc
              ? new OrderSpecifier[] {concert.viewCount.asc(), concert.id.asc()}
              : new OrderSpecifier[] {concert.viewCount.desc(), concert.id.desc()};
      case PRICE ->
          isAsc
              ? new OrderSpecifier[] {concert.minPrice.asc(), concert.id.asc()}
              : new OrderSpecifier[] {concert.minPrice.desc(), concert.id.desc()};
      case TICKET_OPEN_AT ->
          isAsc
              ? new OrderSpecifier[] {concert.bookingStartAt.asc(), concert.id.asc()}
              : new OrderSpecifier[] {concert.bookingStartAt.desc(), concert.id.desc()};
      case CONCERT_DATE ->
          isAsc
              ? new OrderSpecifier[] {concert.startDate.asc(), concert.id.asc()}
              : new OrderSpecifier[] {concert.startDate.desc(), concert.id.desc()};
    };
  }

  private BooleanExpression priceBetween(Integer userMin, Integer userMax) {
    if (userMin == null && userMax == null) {
      return null;
    }
    // 범위가 겹치려면, concert.maxPrice >= userMin || concert.minPrice <= userMax
    if (userMin != null && userMax != null) {
      return concert.maxPrice.goe(userMin).and(concert.minPrice.loe(userMax));
    }

    if (userMax != null) {
      return concert.minPrice.loe(userMax);
    }

    return concert.maxPrice.goe(userMin);
  }

  private BooleanExpression dateBetween(LocalDate userStart, LocalDate userEnd) {
    if (userStart == null && userEnd == null) {
      return null;
    }

    if (userStart != null && userEnd != null) {
      return concert
          .endDate
          .after(userStart.atStartOfDay())
          .and(concert.startDate.before(userEnd.atTime(23, 59, 59)));
    }

    if (userEnd != null) {
      return concert.startDate.before(userEnd.atTime(23, 59, 59));
    }

    return concert.endDate.after(userStart.atStartOfDay());
  }

  private BooleanExpression buildViewCountCondition(ConcertCursor cursor, boolean isAsc) {
    Long value = Long.parseLong(cursor.sortValue());

    if (isAsc) { // 오름차순
      return concert
          .viewCount
          .gt(value)
          .or(concert.viewCount.eq(value).and(concert.id.gt(cursor.id())));
    } else { // 내림차순
      return concert
          .viewCount
          .lt(value)
          .or(concert.viewCount.eq(value).and(concert.id.lt(cursor.id())));
    }
  }

  private BooleanExpression buildPriceCondition(ConcertCursor cursor, boolean isAsc) {
    Integer value = Integer.parseInt(cursor.sortValue());
    if (isAsc) {
      return concert
          .minPrice
          .gt(value)
          .or(concert.minPrice.eq(value).and(concert.id.gt(cursor.id())));
    } else {
      return concert
          .minPrice
          .lt(value)
          .or(concert.minPrice.eq(value).and(concert.id.lt(cursor.id())));
    }
  }

  private BooleanExpression buildTicketOpenAtCondition(ConcertCursor cursor, boolean isAsc) {
    LocalDateTime value = LocalDateTime.parse(cursor.sortValue());
    if (isAsc) {
      return concert
          .bookingStartAt
          .gt(value)
          .or(concert.bookingStartAt.eq(value).and(concert.id.gt(cursor.id())));
    } else {
      return concert
          .bookingStartAt
          .lt(value)
          .or(concert.bookingStartAt.eq(value).and(concert.id.lt(cursor.id())));
    }
  }

  private BooleanExpression buildConcertDateCondition(ConcertCursor cursor, boolean isAsc) {
    LocalDateTime value = LocalDateTime.parse(cursor.sortValue());
    if (isAsc) {
      return concert
          .startDate
          .gt(value)
          .or(concert.startDate.eq(value).and(concert.id.gt(cursor.id())));
    } else {
      return concert
          .startDate
          .lt(value)
          .or(concert.startDate.eq(value).and(concert.id.lt(cursor.id())));
    }
  }
}
