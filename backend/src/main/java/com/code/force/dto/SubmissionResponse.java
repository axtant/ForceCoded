package com.code.force.dto;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        String problemSlug,
        String language,
        String verdict,
        LocalDateTime createdAt
) {}
