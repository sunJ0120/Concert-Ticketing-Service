package com.example.be.common.service;

import com.example.be.common.dto.MainPageResponse;
import com.example.be.concert.dto.ConcertSummaryDto;
import com.example.be.concert.enums.ConcertStatus;
import com.example.be.concert.repository.ConcertRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageService {

  private final ConcertRepository concertRepository;

  public MainPageResponse getMainPageData() {
    long count = concertRepository.countByConcertStatus(ConcertStatus.BOOKING_OPEN);

    List<ConcertSummaryDto> featured =
        concertRepository
            .findTop10ByConcertStatusInOrderByViewCountDesc(
                List.of(ConcertStatus.BOOKING_OPEN, ConcertStatus.SCHEDULED))
            .stream()
            .map(ConcertSummaryDto::from)
            .toList();

    return new MainPageResponse(count, featured);
  }
}
