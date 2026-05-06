package com.code.force.dto;

import java.time.LocalDateTime;

public record ContestSummaryResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        int problemCount
) {}
