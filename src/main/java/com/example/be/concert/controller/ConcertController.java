package com.example.be.concert.controller;

import com.example.be.concert.dto.ConcertDetailResponse;
import com.example.be.concert.dto.MainPageResponse;
import com.example.be.concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Concert", description = "공연 API")
@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
public class ConcertController {

  private final ConcertService concertService;

  @Operation(summary = "메인 페이지", description = "메인 페이지에 필요한 데이터")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "메인 페이지 조회 성공")})
  @GetMapping("/main")
  public ResponseEntity<MainPageResponse> main() {
    return ResponseEntity.ok(concertService.getMainPageData());
  }

  @Operation(summary = "메인 페이지", description = "메인 페이지에 필요한 데이터")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "메인 페이지 조회 성공")})
  @GetMapping("/{id}")
  public ResponseEntity<ConcertDetailResponse> detail(@PathVariable Long id) {
    return ResponseEntity.ok(concertService.getDetailPageData(id));
  }
}
