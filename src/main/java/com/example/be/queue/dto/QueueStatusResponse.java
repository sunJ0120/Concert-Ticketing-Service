package com.example.be.queue.dto;

public record QueueStatusResponse(
    Long rank, Long waitingAhead, Long totalWaiting, Long estimatedWaitSeconds, String status) {

  public static QueueStatusResponse waiting(
      Long rank, Long totalWaiting, Long estimatedWaitSeconds) {
    return new QueueStatusResponse(rank, rank - 1, totalWaiting, estimatedWaitSeconds, "WAITING");
  }

  public static QueueStatusResponse active() {
    return new QueueStatusResponse(0L, 0L, 0L, 0L, "ACTIVE");
  }
}
