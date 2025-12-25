package com.example.be.concert.dto;

import com.example.be.common.exception.InvalidCursorException;
import com.example.be.concert.enums.ConcertSortType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record ConcertCursor(ConcertSortType sortType, String sortValue, Long id) {

  public static ConcertCursor decode(String encoded) {
    if (encoded == null || encoded.isEmpty()) {
      return null;
    }

    try {
      String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
      String[] split = decoded.split(",");
      return new ConcertCursor(ConcertSortType.from(split[0]), split[1], Long.parseLong(split[2]));
    } catch (InvalidCursorException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidCursorException();
    }
  }

  public static String encode(ConcertSortType sortType, Object sortValue, Long id) {
    String raw = sortType.getValue() + "," + sortValue.toString() + "," + id;
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}
