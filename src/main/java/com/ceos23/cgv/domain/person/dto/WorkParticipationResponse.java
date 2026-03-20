package com.ceos23.cgv.domain.person.dto;

import com.ceos23.cgv.domain.person.entity.WorkParticipation;
import com.ceos23.cgv.domain.person.enums.PersonType;
import com.ceos23.cgv.domain.person.enums.RoleType;

public record WorkParticipationResponse(
        Long participationId,
        Long personId,
        String personName,
        PersonType personType,
        RoleType role
) {
    public static WorkParticipationResponse from(WorkParticipation participation) {
        return new WorkParticipationResponse(
                participation.getId(),
                participation.getPerson().getId(),
                participation.getPerson().getName(),
                participation.getPerson().getType(),
                participation.getRole()
        );
    }
}