package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.enums.TheaterType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TheaterCreateRequest {
    private String name;        // 예: "1관", "IMAX관"
    private TheaterType type;   // 예: NORMAL, IMAX 등
    private int seatCount;      // 예: 120
}