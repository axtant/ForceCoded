package com.code.force.controller;

import com.code.force.dto.SubmissionDetailResponse;
import com.code.force.dto.SubmissionRequest;
import com.code.force.dto.SubmissionResponse;
import com.code.force.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(@RequestBody SubmissionRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Submission request: user={} problem={} language={}", userDetails.getUsername(), request.problemSlug(), request.language());
        SubmissionResponse response = submissionService.createSubmission(request, userDetails.getUsername());
        log.info("Submission queued: id={}", response.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDetailResponse> getSubmission(@PathVariable Long id) {
        log.debug("Fetch submission: id={}", id);
        return ResponseEntity.ok(submissionService.getSubmission(id));
    }

    @GetMapping
    public ResponseEntity<List<SubmissionResponse>> getByProblem(@RequestParam Long problemId) {
        log.debug("Fetch submissions for problemId={}", problemId);
        return ResponseEntity.ok(submissionService.getSubmissionsByProblem(problemId));
    }
}
