package com.code.force.service;

import com.code.force.dto.*;
import com.code.force.model.*;
import com.code.force.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public ContestService(ContestRepository contestRepository,
                          ContestProblemRepository contestProblemRepository,
                          ProblemRepository problemRepository,
                          SubmissionRepository submissionRepository) {
        this.contestRepository = contestRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<ContestSummaryResponse> getAll() {
        return contestRepository.findAllByOrderByStartTimeDesc().stream()
                .map(c -> {
                    int count = contestProblemRepository.findByContestIdOrderByOrderIndex(c.getId()).size();
                    return toSummary(c, count);
                })
                .toList();
    }

    public ContestDetailResponse getById(Long id) {
        Contest contest = findContest(id);
        List<ContestProblemDto> problems = contestProblemRepository
                .findByContestIdOrderByOrderIndex(id)
                .stream()
                .map(cp -> new ContestProblemDto(
                        cp.getLabel(),
                        cp.getOrderIndex(),
                        cp.getProblem().getId(),
                        cp.getProblem().getSlug(),
                        cp.getProblem().getTitle(),
                        cp.getProblem().getDifficulty().name()
                ))
                .toList();
        return new ContestDetailResponse(
                contest.getId(), contest.getTitle(), contest.getDescription(),
                contest.getStartTime(), contest.getEndTime(),
                contest.getStatus().name(), problems
        );
    }

    public List<StandingsRow> getStandings(Long contestId) {
        Contest contest = findContest(contestId);
        List<ContestProblem> contestProblems =
                contestProblemRepository.findByContestIdOrderByOrderIndex(contestId);
        List<Submission> submissions =
                submissionRepository.findByContestIdOrderByCreatedAtAsc(contestId);

        // userId → problemId → ordered submissions
        Map<UUID, Map<Long, List<Submission>>> byUserByProblem = new LinkedHashMap<>();
        Map<UUID, String> usernameMap = new LinkedHashMap<>();

        for (Submission s : submissions) {
            UUID userId = s.getUser().getId();
            Long problemId = s.getProblem().getId();
            usernameMap.putIfAbsent(userId, s.getUser().getUsername());
            byUserByProblem
                    .computeIfAbsent(userId, k -> new HashMap<>())
                    .computeIfAbsent(problemId, k -> new ArrayList<>())
                    .add(s);
        }

        List<StandingsRow> rows = new ArrayList<>();
        for (UUID userId : usernameMap.keySet()) {
            Map<Long, List<Submission>> problemSubs =
                    byUserByProblem.getOrDefault(userId, Map.of());
            int solved = 0;
            long totalPenalty = 0;
            List<ProblemStandingDto> problemDtos = new ArrayList<>();

            for (ContestProblem cp : contestProblems) {
                Long problemId = cp.getProblem().getId();
                List<Submission> subs = problemSubs.getOrDefault(problemId, List.of());

                int wrongBefore = 0;
                Long penaltyMinutes = null;
                boolean isSolved = false;

                for (Submission s : subs) {
                    if (s.getVerdict() == Verdict.ACCEPTED) {
                        long mins = ChronoUnit.MINUTES.between(
                                contest.getStartTime(), s.getCreatedAt());
                        penaltyMinutes = mins + 20L * wrongBefore;
                        isSolved = true;
                        break;
                    } else if (s.getVerdict() != Verdict.IN_PROGRESS
                            && s.getVerdict() != Verdict.QUEUED) {
                        wrongBefore++;
                    }
                }

                if (isSolved) {
                    solved++;
                    totalPenalty += penaltyMinutes;
                }

                problemDtos.add(new ProblemStandingDto(
                        cp.getLabel(), isSolved, wrongBefore, penaltyMinutes));
            }

            rows.add(new StandingsRow(0, usernameMap.get(userId), solved, totalPenalty, problemDtos));
        }

        rows.sort(Comparator.comparingInt(StandingsRow::solved).reversed()
                .thenComparingLong(StandingsRow::penalty));

        List<StandingsRow> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            StandingsRow r = rows.get(i);
            ranked.add(new StandingsRow(i + 1, r.username(), r.solved(), r.penalty(), r.problems()));
        }
        return ranked;
    }

    public ContestSummaryResponse create(ContestCreateRequest req) {
        Contest contest = new Contest();
        contest.setTitle(req.title());
        contest.setDescription(req.description());
        contest.setStartTime(req.startTime());
        contest.setEndTime(req.endTime());
        contest.setStatus(resolveStatus(req.startTime(), req.endTime()));
        contestRepository.save(contest);
        return toSummary(contest, 0);
    }

    public ContestDetailResponse addProblem(Long contestId, AddProblemRequest req) {
        Contest contest = findContest(contestId);
        Problem problem = problemRepository.findBySlug(req.problemSlug())
                .orElseThrow(() -> new NoSuchElementException("Problem not found: " + req.problemSlug()));

        ContestProblem cp = new ContestProblem();
        cp.setContest(contest);
        cp.setProblem(problem);
        cp.setLabel(req.label());
        cp.setOrderIndex(req.orderIndex());
        contestProblemRepository.save(cp);

        return getById(contestId);
    }

    private Contest findContest(Long id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contest not found: " + id));
    }

    private ContestSummaryResponse toSummary(Contest c, int problemCount) {
        return new ContestSummaryResponse(
                c.getId(), c.getTitle(), c.getDescription(),
                c.getStartTime(), c.getEndTime(),
                c.getStatus().name(), problemCount
        );
    }

    private ContestStatus resolveStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) return ContestStatus.UPCOMING;
        if (now.isAfter(end))    return ContestStatus.FINISHED;
        return ContestStatus.ACTIVE;
    }
}
