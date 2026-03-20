package com.ceos23.cgv.domain.person.dto;

import com.ceos23.cgv.domain.person.enums.PersonType;
import java.time.LocalDate;

public record PersonCreateRequest(
        PersonType type,
        String name,
        String englishName,
        LocalDate birthDate,
        String award
) {
}