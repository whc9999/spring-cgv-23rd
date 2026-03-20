package com.ceos23.cgv.domain.person.dto;

import com.ceos23.cgv.domain.person.enums.RoleType;

public record WorkParticipationRequest(
        Long movieId,
        Long personId,
        RoleType role
) {
}