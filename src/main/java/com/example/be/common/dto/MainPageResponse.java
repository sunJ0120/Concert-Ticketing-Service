package com.example.be.common.dto;

import com.example.be.concert.dto.ConcertSummaryDto;
import java.util.List;

public record MainPageResponse(long openCount, List<ConcertSummaryDto> featuredConcerts) {}
