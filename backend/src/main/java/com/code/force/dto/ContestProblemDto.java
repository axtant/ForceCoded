package com.code.force.dto;

public record ContestProblemDto(
        String label,
        int orderIndex,
        Long problemId,
        String slug,
        String title,
        String difficulty
) {}
