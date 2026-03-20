package com.ceos23.cgv.domain.cinema.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.entity.CinemaLike;
import com.ceos23.cgv.domain.cinema.repository.CinemaLikeRepository;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaLikeService {

    private final CinemaLikeRepository cinemaLikeRepository;
    private final UserRepository userRepository;
    private final CinemaRepository cinemaRepository;

    /**
     * [POST] 극장 찜하기 토글 (없으면 생성, 있으면 삭제)
     */
    @Transactional
    public String toggleCinemaLike(Long userId, Long cinemaId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));

        // 기존에 자주 가는 극장으로 등록했는지 확인
        Optional<CinemaLike> existingLike = cinemaLikeRepository.findByUserIdAndCinemaId(userId, cinemaId);

        if (existingLike.isPresent()) {
            // 이미 등록된 상태라면 -> 찜 취소 (삭제)
            cinemaLikeRepository.delete(existingLike.get());
            return "자주 가는 극장이 취소되었습니다.";
        } else {
            // 안 등록된 상태라면 -> 찜 생성 (저장)
            CinemaLike newLike = CinemaLike.builder()
                    .user(user)
                    .cinema(cinema)
                    .build();
            cinemaLikeRepository.save(newLike);
            return "자주 가는 극장으로 등록되었습니다.";
        }
    }

    /**
     * [GET] 특정 유저가 찜한(자주 가는) 극장 목록 조회
     */
    public List<CinemaLike> getLikedCinemasByUser(Long userId) {
        return cinemaLikeRepository.findByUserId(userId);
    }
}