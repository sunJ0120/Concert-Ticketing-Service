package com.example.be.auth.dto;

public record OAuthUserInfo(String provider, String providerId, String email, String name) {}
