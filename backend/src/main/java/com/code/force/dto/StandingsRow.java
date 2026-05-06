package com.code.force.dto;

import java.util.List;

public record StandingsRow(
        int rank,
        String username,
        int solved,
        long penalty,
        List<ProblemStandingDto> problems
) {}
