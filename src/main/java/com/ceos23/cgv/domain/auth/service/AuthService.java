package com.ceos23.cgv.domain.auth.service;

import com.ceos23.cgv.domain.auth.dto.*;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.enums.Role;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입되어 있는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
    }

    /**
     * 로그인 로직
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        Long userId = Long.parseLong(authentication.getName());

        String accessToken = tokenProvider.createAccessToken(userId, authentication);
        String refreshToken = tokenProvider.createRefreshToken(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.updateRefreshToken(refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 토큰 재발급 로직
     */
    @Transactional
    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        // 1. Refresh Token 자체의 유효성 검증
        if (!tokenProvider.validateAccessToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 입니다.");
        }

        // 2. 토큰에서 유저 ID 추출
        Long userId = Long.parseLong(tokenProvider.getTokenUserId(refreshToken));

        // 3. DB에서 유저를 찾고, DB에 저장된 토큰과 클라이언트가 보낸 토큰이 일치하는지 대조
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("토큰 정보가 일치하지 않습니다. (탈취 의심)");
        }

        // 4. 검증을 통과했다면 새로운 토큰들을 생성
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(user.getId()), "",
                java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
        );

        String newAccessToken = tokenProvider.createAccessToken(userId, authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(userId); // RTR 사용 (리프레쉬 토큰도 새로 발급)

        // 5. DB의 Refresh Token 업데이트
        user.updateRefreshToken(newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}