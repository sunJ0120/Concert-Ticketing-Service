package com.example.be.concert.dto;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> content, String nextCursor, boolean hasNext, int size, int returnedCount) {

  public static <T> CursorPageResponse<T> of(
      List<T> content, String nextCursor, boolean hasNext, int size) {
    return new CursorPageResponse<>(content, nextCursor, hasNext, size, content.size());
  }
}
