package com.ceos23.cgv.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider implements InitializingBean {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long REFRESH_TOKEN_VALIDITY_MULTIPLIER = 24L * 14L;

    private final String secret;
    private final long tokenValidityInMilliseconds;
    private final UserDetailsService userDetailsService;
    private Key key;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds,
            UserDetailsService userDetailsService) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. HttpServletRequest에서 토큰 추출
    public String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // 2. 토큰 생성
    public String createAccessToken(Long id, Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(id)) // Payload: 유저 식별자 (ID)
                .claim("auth", authorities)     // Payload: 권한 (ROLE_USER, ROLE_ADMIN 등)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    // 3. 토큰에서 유저 ID(Subject) 추출
    public String getTokenUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 4. Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        // 1. 토큰 복호화 (Claims 추출)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 2. 권한 정보 추출 ("ROLE_USER" 등)
        Collection<? extends GrantedAuthority> authorities =
                java.util.Arrays.stream(claims.get("auth").toString().split(","))
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toList());

        // 3. UserDetails 객체 생성 (DB 조회 없이 토큰에 있는 유저 ID와 권한 정보만으로 생성!)
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                claims.getSubject(),
                "",
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    // 5. 토큰 유효성 검증
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature. type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT. type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT. type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
        }
        return false;
    }

    // 리프레쉬 토큰 생성 메서드
    public String createRefreshToken(Long id) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds * REFRESH_TOKEN_VALIDITY_MULTIPLIER);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public long getRefreshTokenValidityInSeconds() {
        return tokenValidityInMilliseconds / 1000 * REFRESH_TOKEN_VALIDITY_MULTIPLIER;
    }
}
