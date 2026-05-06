package com.code.force.controller;

import com.code.force.dto.ProblemDetailResponse;
import com.code.force.dto.ProblemSummaryResponse;
import com.code.force.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemSummaryResponse>> getAllProblems() {
        log.debug("Fetch all problems");
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProblemDetailResponse> getProblem(@PathVariable String slug) throws Exception {
        log.info("Fetch problem: slug={}", slug);
        return ResponseEntity.ok(problemService.getProblemBySlug(slug));
    }
}
