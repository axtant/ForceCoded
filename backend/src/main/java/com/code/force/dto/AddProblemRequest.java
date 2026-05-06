package com.code.force.dto;

public record AddProblemRequest(
        String problemSlug,
        String label,
        int orderIndex
) {}
