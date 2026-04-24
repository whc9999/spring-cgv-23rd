package com.ceos23.cgv.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (JWT 사용 시 일반적으로 비활성화)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 세션 관리를 Stateless로 설정 (서버가 세션을 저장하지 않음)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // 3. 권한별 URL 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입 API는 누구나 접근 가능
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger UI 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 🚀 핵심: 관리자 전용 경로는 ADMIN 권한이 있어야만 접근 가능
                        // Security 내부적으로 "ROLE_" 접두사를 붙여서 검사하므로 "ADMIN"이라고 적습니다.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 영화, 상영관 단순 조회 API는 비로그인 유저도 접근 가능하게 열어두는 예시
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**", "/api/v1/cinemas/**").permitAll()

                        // 그 외 모든 요청(예매, 결제 등)은 로그인(인증)된 유저만 접근 가능
                        .anyRequest().authenticated()
                )

                // 4. 커스텀 JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화를 위한 Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
