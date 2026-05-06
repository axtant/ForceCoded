package com.code.force.dto;

import java.time.LocalDateTime;

public record ContestCreateRequest(
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
