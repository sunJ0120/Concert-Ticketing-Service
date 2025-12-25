package com.example.be.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
  INVALID_CURSOR(HttpStatus.BAD_REQUEST, "C001", "잘못된 커서 형식입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
