package com.ceos23.cgv.domain.photo.service;

import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.repository.MovieRepository;
import com.ceos23.cgv.domain.person.entity.Person;
import com.ceos23.cgv.domain.person.repository.PersonRepository;
import com.ceos23.cgv.domain.photo.dto.PhotoCreateRequest;
import com.ceos23.cgv.domain.photo.entity.Photo;
import com.ceos23.cgv.domain.photo.repository.PhotoRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;

    /**
     * [POST] 사진 등록 (영화 사진 or 인물 사진)
     */
    @Transactional
    public Photo createPhoto(PhotoCreateRequest request) {
        Movie movie = null;
        if (request.movieId() != null) {
            movie = movieRepository.findById(request.movieId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
        }

        Person person = null;
        if (request.personId() != null) {
            person = personRepository.findById(request.personId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PERSON_NOT_FOUND));
        }

        // 방어 로직: 둘 다 null이면 사진을 어디에 연결할지 알 수 없으므로 에러 처리
        if (movie == null && person == null) {
            throw new IllegalArgumentException("영화 ID 또는 인물 ID 중 하나는 필수입니다.");
        }

        Photo photo = Photo.builder()
                .movie(movie)
                .person(person)
                .name(request.name())
                .build();

        return photoRepository.save(photo);
    }

    /**
     * [GET] 특정 영화의 사진 목록 조회
     */
    public List<Photo> getPhotosByMovieId(Long movieId) {
        return photoRepository.findByMovieId(movieId);
    }

    /**
     * [GET] 특정 인물의 사진 목록 조회
     */
    public List<Photo> getPhotosByPersonId(Long personId) {
        return photoRepository.findByPersonId(personId);
    }
}