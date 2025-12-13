package com.example.be.auth.dto;

public record SignupRequest(
    String email,
    String password,
    String name,
    String phone
) {

}
