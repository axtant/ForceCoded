package com.code.force.repository;

import com.code.force.model.Contest;
import com.code.force.model.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByStatus(ContestStatus status);
    List<Contest> findAllByOrderByStartTimeDesc();
}
