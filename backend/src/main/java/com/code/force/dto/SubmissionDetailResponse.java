package com.code.force.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionDetailResponse(
        Long id,
        String problemSlug,
        String language,
        String verdict,
        Integer executionTimeMs,
        LocalDateTime createdAt,
        List<TestCaseResultDto> results
) {}
