package com.code.force.repository;

import com.code.force.model.SubmissionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionResultRepository extends JpaRepository<SubmissionResult, Long> {
    List<SubmissionResult> findBySubmissionIdOrderByTestCaseIndex(Long submissionId);
}
