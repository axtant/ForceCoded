package com.code.force.service;

import com.code.force.dto.SubmissionDetailResponse;
import com.code.force.dto.SubmissionRequest;
import com.code.force.dto.SubmissionResponse;
import com.code.force.dto.TestCaseResultDto;
import com.code.force.model.*;
import com.code.force.repository.ContestRepository;
import com.code.force.repository.ProblemRepository;
import com.code.force.repository.SubmissionRepository;
import com.code.force.repository.SubmissionResultRepository;
import com.code.force.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository submissionResultRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ContestRepository contestRepository;
    private final JudgeService judgeService;

    public SubmissionService(SubmissionRepository submissionRepository,
                             SubmissionResultRepository submissionResultRepository,
                             ProblemRepository problemRepository,
                             UserRepository userRepository,
                             ContestRepository contestRepository,
                             JudgeService judgeService) {
        this.submissionRepository = submissionRepository;
        this.submissionResultRepository = submissionResultRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.contestRepository = contestRepository;
        this.judgeService = judgeService;
    }

    public SubmissionResponse createSubmission(SubmissionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Problem problem = problemRepository.findBySlug(request.problemSlug())
                .orElseThrow(() -> new RuntimeException("Problem not found: " + request.problemSlug()));

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(Language.valueOf(request.language().toUpperCase()));
        submission.setCode(request.code());
        submission.setVerdict(Verdict.QUEUED);
        if (request.contestId() != null) {
            contestRepository.findById(request.contestId())
                    .ifPresent(submission::setContest);
        }
        submissionRepository.save(submission);
        log.info("Submission saved: id={} user={} problem={} language={} contestId={}",
                submission.getId(), username, request.problemSlug(), request.language(), request.contestId());

        judgeService.evaluate(submission);

        return toResponse(submission);
    }

    public SubmissionDetailResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + id));

        List<TestCaseResultDto> results = submissionResultRepository
                .findBySubmissionIdOrderByTestCaseIndex(id)
                .stream()
                .map(r -> new TestCaseResultDto(r.getTestCaseIndex(), r.getVerdict().name(), r.getExecutionTimeMs()))
                .toList();

        return new SubmissionDetailResponse(
                submission.getId(),
                submission.getProblem().getSlug(),
                submission.getLanguage().name(),
                submission.getVerdict().name(),
                submission.getExecutionTimeMs(),
                submission.getCreatedAt(),
                results
        );
    }

    public List<SubmissionResponse> getSubmissionsByProblem(Long problemId) {
        return submissionRepository.findByProblemId(problemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SubmissionResponse toResponse(Submission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getProblem().getSlug(),
                s.getLanguage().name(),
                s.getVerdict().name(),
                s.getCreatedAt()
        );
    }
}
