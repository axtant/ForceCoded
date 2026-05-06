package com.code.force.service;

import com.code.force.dto.ProblemDetailResponse;
import com.code.force.dto.ProblemSummaryResponse;
import com.code.force.model.Problem;
import com.code.force.model.Verdict;
import com.code.force.repository.ProblemRepository;
import com.code.force.repository.SubmissionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemFileService problemFileService;

    public ProblemService(ProblemRepository problemRepository,
                          SubmissionRepository submissionRepository,
                          ProblemFileService problemFileService) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.problemFileService = problemFileService;
    }

    // GET /api/problems — list all problems with solved count
    public List<ProblemSummaryResponse> getAllProblems() {
        return problemRepository.findAll().stream()
                .map(p -> new ProblemSummaryResponse(
                        p.getId(),
                        p.getSlug(),
                        p.getTitle(),
                        p.getDifficulty(),
                        getSolvedCount(p.getId())
                ))
                .toList();
    }

    // GET /api/problems/{slug} — full detail with statement + sample tests
    public ProblemDetailResponse getProblemBySlug(String slug) throws Exception {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + slug));

        JsonNode meta       = problemFileService.loadMetadata(slug);
        String statement    = problemFileService.loadStatement(slug);
        var sampleTests     = problemFileService.loadSampleTests(slug);

        return new ProblemDetailResponse(
                problem.getId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getDifficulty(),
                statement,
                meta.get("time_limit_ms").asInt(),
                meta.get("memory_limit_mb").asInt(),
                getSolvedCount(problem.getId()),
                sampleTests
        );
    }

    private long getSolvedCount(Long problemId) {
        return submissionRepository.countDistinctUsersByProblemIdAndVerdict(problemId, Verdict.ACCEPTED);
    }
}
