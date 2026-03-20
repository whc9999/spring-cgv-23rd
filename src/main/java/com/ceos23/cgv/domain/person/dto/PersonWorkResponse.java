package com.ceos23.cgv.domain.person.dto;

import com.ceos23.cgv.domain.person.entity.WorkParticipation;
import com.ceos23.cgv.domain.person.enums.RoleType;

public record PersonWorkResponse(
        Long participationId,
        Long movieId,
        String movieTitle,
        RoleType role
) {
    public static PersonWorkResponse from(WorkParticipation participation) {
        return new PersonWorkResponse(
                participation.getId(),
                participation.getMovie().getId(),
                participation.getMovie().getTitle(),
                participation.getRole()
        );
    }
}