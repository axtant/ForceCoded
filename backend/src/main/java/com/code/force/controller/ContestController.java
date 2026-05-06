package com.code.force.controller;

import com.code.force.dto.*;
import com.code.force.service.ContestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @GetMapping
    public ResponseEntity<List<ContestSummaryResponse>> getAll() {
        return ResponseEntity.ok(contestService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getById(id));
    }

    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingsRow>> getStandings(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getStandings(id));
    }

    @PostMapping
    public ResponseEntity<ContestSummaryResponse> create(@RequestBody ContestCreateRequest req) {
        return ResponseEntity.ok(contestService.create(req));
    }

    @PostMapping("/{id}/problems")
    public ResponseEntity<ContestDetailResponse> addProblem(
            @PathVariable Long id,
            @RequestBody AddProblemRequest req) {
        return ResponseEntity.ok(contestService.addProblem(id, req));
    }
}
