package com.example.be.concert.service;

import com.example.be.common.exception.BusinessException;
import com.example.be.concert.domain.Concert;
import com.example.be.concert.dto.ConcertDetailResponse;
import com.example.be.concert.dto.ConcertSummaryDto;
import com.example.be.concert.dto.MainPageResponse;
import com.example.be.concert.enums.ConcertStatus;
import com.example.be.concert.exception.ConcertErrorCode;
import com.example.be.concert.repository.ConcertRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertService {

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

  @Transactional
  public ConcertDetailResponse getDetailPageData(Long concertId) {
    Concert concert =
        concertRepository
            .findByIdWithDetail(concertId)
            .orElseThrow(() -> new BusinessException(ConcertErrorCode.CONCERT_NOT_FOUND));
    concert.incrementViewCount(); // 조회수 상승
    return ConcertDetailResponse.from(concert);
  }
}
