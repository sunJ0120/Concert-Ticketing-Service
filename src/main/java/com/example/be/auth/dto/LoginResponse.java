package com.example.be.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken
) {

}
