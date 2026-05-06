package com.code.force.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestDetailResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        List<ContestProblemDto> problems
) {}
