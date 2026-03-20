package com.ceos23.cgv.domain.user.service;

import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public User join(String name, String email,String nickname) {
        // 1. 닉네임 중복 검증
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 2. 새로운 유저 객체 생성 (Badge는 엔티티에서 설정한 기본값 NORMAL)
        User newUser = User.builder()
                .name(name)
                .email(email)
                .nickname(nickname)
                .build();

        // 3. DB에 저장 후 반환
        return userRepository.save(newUser);
    }

    /**
     * 유저 단건 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
