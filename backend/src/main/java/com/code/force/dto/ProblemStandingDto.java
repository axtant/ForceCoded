package com.code.force.dto;

public record ProblemStandingDto(
        String label,
        boolean solved,
        int attempts,
        Long penaltyMinutes
) {}
