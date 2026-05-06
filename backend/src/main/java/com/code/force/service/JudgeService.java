package com.code.force.service;

import com.code.force.judge.CodeEnricher;
import com.code.force.judge.JudgeEngine;
import com.code.force.judge.SandboxResult;
import com.code.force.model.Submission;
import com.code.force.model.SubmissionResult;
import com.code.force.model.Verdict;
import com.code.force.repository.SubmissionRepository;
import com.code.force.repository.SubmissionResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Slf4j
@Service
public class JudgeService {

    private final JudgeEngine judgeEngine;
    private final CodeEnricher codeEnricher;
    private final ProblemFileService problemFileService;
    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository submissionResultRepository;
    private final WebSocketMessenger wsMessenger;
    private final ObjectMapper objectMapper;

    public JudgeService(JudgeEngine judgeEngine, CodeEnricher codeEnricher,
                        ProblemFileService problemFileService,
                        SubmissionRepository submissionRepository,
                        SubmissionResultRepository submissionResultRepository,
                        WebSocketMessenger wsMessenger, ObjectMapper objectMapper) {
        this.judgeEngine = judgeEngine;
        this.codeEnricher = codeEnricher;
        this.problemFileService = problemFileService;
        this.submissionRepository = submissionRepository;
        this.submissionResultRepository = submissionResultRepository;
        this.wsMessenger = wsMessenger;
        this.objectMapper = objectMapper;
    }

    @Async("judgeExecutor")
    public void evaluate(Submission submission) {
        long submissionId = submission.getId();
        Path tempDir = null;
        try {
            String slug     = submission.getProblem().getSlug();
            String language = submission.getLanguage().name();
            log.info("Judge started: submissionId={} problem={} language={}", submissionId, slug, language);

            submission.setVerdict(Verdict.IN_PROGRESS);
            submissionRepository.save(submission);
            wsMessenger.sendFinalVerdict(submissionId, "IN_PROGRESS");

            JsonNode meta       = problemFileService.loadMetadata(slug);
            int timeLimitMs     = meta.get("time_limit_ms").asInt();
            int memoryLimitMb   = meta.get("memory_limit_mb").asInt();
            int totalTests      = meta.get("total_tests").asInt();
            log.debug("Judge config: submissionId={} tests={} timeLimitMs={} memoryLimitMb={}", submissionId, totalTests, timeLimitMs, memoryLimitMb);

            tempDir = Files.createTempDirectory("judge-" + submissionId + "-");

            problemFileService.copyTestCasesToDir(slug, tempDir.resolve("testcases"));

            if ("JAVASCRIPT".equals(language)) {
                codeEnricher.prepareJs(tempDir, submission.getCode());
            } else {
                codeEnricher.prepareJava(tempDir, submission.getCode());
            }

            SandboxResult result = judgeEngine.execute(tempDir, language, totalTests, timeLimitMs, memoryLimitMb);

            if (result.timedOut) {
                log.info("Judge result: submissionId={} verdict=TIME_LIMIT_EXCEEDED", submissionId);
                setFinalVerdict(submission, Verdict.TIME_LIMIT_EXCEEDED);
                return;
            }
            if (result.oomKilled) {
                log.info("Judge result: submissionId={} verdict=MEMORY_LIMIT_EXCEEDED", submissionId);
                setFinalVerdict(submission, Verdict.MEMORY_LIMIT_EXCEEDED);
                return;
            }

            JsonNode root = objectMapper.readTree(result.output);

            if (root.has("compile_error")) {
                log.info("Judge result: submissionId={} verdict=COMPILATION_ERROR", submissionId);
                setFinalVerdict(submission, Verdict.COMPILATION_ERROR);
                return;
            }

            Verdict overallVerdict = Verdict.ACCEPTED;
            int maxTimeMs = 0;

            for (JsonNode testNode : root) {
                int testIndex = testNode.get("test").asInt();
                String status = testNode.get("status").asText();
                int timeMs    = testNode.get("time_ms").asInt();

                Verdict testVerdict = mapStatus(status);
                maxTimeMs = Math.max(maxTimeMs, timeMs);
                log.debug("Test case: submissionId={} test={} status={} timeMs={}", submissionId, testIndex, status, timeMs);

                SubmissionResult sr = new SubmissionResult();
                sr.setSubmission(submission);
                sr.setTestCaseIndex(testIndex);
                sr.setVerdict(testVerdict);
                sr.setExecutionTimeMs(timeMs);
                submissionResultRepository.save(sr);

                wsMessenger.sendTestResult(submissionId, testIndex, status);

                if (testVerdict != Verdict.ACCEPTED && overallVerdict == Verdict.ACCEPTED) {
                    overallVerdict = testVerdict;
                }
            }

            submission.setVerdict(overallVerdict);
            submission.setExecutionTimeMs(maxTimeMs);
            submissionRepository.save(submission);
            wsMessenger.sendFinalVerdict(submissionId, overallVerdict.name());
            log.info("Judge complete: submissionId={} verdict={} maxTimeMs={}", submissionId, overallVerdict, maxTimeMs);

        } catch (Exception e) {
            log.error("Judge failed: submissionId={} error={}", submissionId, e.getMessage(), e);
            submission.setVerdict(Verdict.RUNTIME_ERROR);
            submissionRepository.save(submission);
            wsMessenger.sendFinalVerdict(submissionId, "RUNTIME_ERROR");
        } finally {
            if (tempDir != null) deleteDir(tempDir);
        }
    }

    private void setFinalVerdict(Submission submission, Verdict verdict) {
        submission.setVerdict(verdict);
        submissionRepository.save(submission);
        wsMessenger.sendFinalVerdict(submission.getId(), verdict.name());
    }

    private Verdict mapStatus(String status) {
        return switch (status) {
            case "AC"  -> Verdict.ACCEPTED;
            case "WA"  -> Verdict.WRONG_ANSWER;
            case "TLE" -> Verdict.TIME_LIMIT_EXCEEDED;
            case "MLE" -> Verdict.MEMORY_LIMIT_EXCEEDED;
            case "RE"  -> Verdict.RUNTIME_ERROR;
            default    -> Verdict.RUNTIME_ERROR;
        };
    }

    private void deleteDir(Path dir) {
        try {
            Files.walk(dir)
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        } catch (IOException ignored) {}
    }
}
