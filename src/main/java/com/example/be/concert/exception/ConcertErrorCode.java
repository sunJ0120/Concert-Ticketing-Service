package com.example.be.concert.exception;

import com.example.be.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ConcertErrorCode implements ErrorCode {
  CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONCERT_001", "존재하지 않는 공연입니다");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
