package com.ceos23.cgv.domain.auth.service;

import com.ceos23.cgv.domain.auth.dto.*;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import com.ceos23.cgv.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    /**
     * 회원가입 로직
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        User savedUser = userRepository.save(createUser(request));

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
    }

    private void validateSignupRequest(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private User createUser(SignupRequest request) {
        return User.create(
                request.name(),
                request.email(),
                request.nickname(),
                passwordEncoder.encode(request.password())
        );
    }

    /**
     * 로그인 로직
     */
    @Transactional
    public TokenPair login(LoginRequest request) {
        Authentication authentication = authenticate(request.email(), request.password());
        Long userId = Long.parseLong(authentication.getName());

        TokenPair tokenPair = issueTokens(userId, authentication);
        User user = findUser(userId);
        user.updateRefreshToken(tokenPair.refreshToken());

        return tokenPair;
    }

    /**
     * 토큰 재발급 로직
     */
    @Transactional
    public TokenPair reissue(String refreshToken) {
        validateRefreshToken(refreshToken);
        Long userId = Long.parseLong(tokenProvider.getTokenUserId(refreshToken));
        User user = findUser(userId);
        validateStoredRefreshToken(user, refreshToken);

        Authentication authentication = createAuthentication(user);
        TokenPair tokenPair = issueTokens(userId, authentication);
        user.updateRefreshToken(tokenPair.refreshToken());

        return tokenPair;
    }

    private Authentication authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!tokenProvider.validateAccessToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validateStoredRefreshToken(User user, String refreshToken) {
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
    }

    private Authentication createAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(user.getId()),
                "",
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    private TokenPair issueTokens(Long userId, Authentication authentication) {
        String accessToken = tokenProvider.createAccessToken(userId, authentication);
        String refreshToken = tokenProvider.createRefreshToken(userId);

        return new TokenPair(accessToken, refreshToken, tokenProvider.getRefreshTokenValidityInSeconds());
    }
}
