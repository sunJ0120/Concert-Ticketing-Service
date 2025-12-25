package com.example.be.common.controller;

import com.example.be.common.dto.MainPageResponse;
import com.example.be.common.service.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Page", description = "페이지 API")
@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {

  private final PageService pageService;

  @Operation(summary = "메인 페이지", description = "메인 페이지에 필요한 데이터")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "메인 페이지 조회 성공")})
  @GetMapping("/main")
  public ResponseEntity<MainPageResponse> main() {
    return ResponseEntity.ok(pageService.getMainPageData());
  }
}
