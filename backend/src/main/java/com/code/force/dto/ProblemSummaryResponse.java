package com.code.force.dto;

import com.code.force.model.Difficulty;

// returned by GET /api/problems — list view, no statement or test cases
public record ProblemSummaryResponse(
        Long id,
        String slug,
        String title,
        Difficulty difficulty,
        long solvedCount
) {}
