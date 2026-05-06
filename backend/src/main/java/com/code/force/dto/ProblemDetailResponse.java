package com.code.force.dto;

import com.code.force.model.Difficulty;
import com.code.force.service.ProblemFileService.SampleTest;

import java.util.List;

// returned by GET /api/problems/{slug} — full detail with statement and sample tests
public record ProblemDetailResponse(
        Long id,
        String slug,
        String title,
        Difficulty difficulty,
        String statement,
        int timeLimitMs,
        int memoryLimitMb,
        long solvedCount,
        List<SampleTest> sampleTests
) {}
