package com.ceos23.cgv.domain.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long refreshTokenMaxAgeSeconds
) {
    public TokenResponse toResponse() {
        return new TokenResponse(accessToken);
    }
}
