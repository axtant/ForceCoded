package com.code.force.repository;

import com.code.force.model.Submission;
import com.code.force.model.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserId(UUID userId);
    List<Submission> findByProblemId(Long problemId);
    List<Submission> findByUserIdAndProblemId(UUID userId, Long problemId);

    // counts distinct users who solved a problem — used for "solved by X users" display
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Submission s WHERE s.problem.id = :problemId AND s.verdict = :verdict")
    long countDistinctUsersByProblemIdAndVerdict(Long problemId, Verdict verdict);

    List<Submission> findByContestIdOrderByCreatedAtAsc(Long contestId);
}
