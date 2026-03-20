package com.ceos23.cgv.domain.cinema.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CinemaCreateRequest {
    private String name;
    private String region;
    private boolean isSpecial;
}