package com.example.be.auth.dto;

public record LoginRequest(
    String email,
    String password
) {

}
