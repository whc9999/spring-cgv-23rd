package com.ceos23.cgv.domain.auth.controller;

import com.ceos23.cgv.domain.auth.dto.*;
import com.ceos23.cgv.domain.auth.service.AuthService;
import com.ceos23.cgv.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;

    // 1. 회원가입 (BCryptPasswordEncoder로 비밀번호 암호화 후 저장)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    // 2. 로그인 (인증 후 Token 발급)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokenPair = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenPair).toString())
                .body(ApiResponse.success(tokenPair.toResponse()));
    }

    // 3. 토큰 재발급 (Access Token이 만료되었을 때 호출)
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        TokenPair tokenPair = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenPair).toString())
                .body(ApiResponse.success(tokenPair.toResponse()));
    }

    private ResponseCookie createRefreshTokenCookie(TokenPair tokenPair) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(tokenPair.refreshTokenMaxAgeSeconds())
                .sameSite("Lax")
                .build();
    }
}
