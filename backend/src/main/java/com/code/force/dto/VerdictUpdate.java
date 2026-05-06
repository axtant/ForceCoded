package com.code.force.dto;

// pushed over WebSocket to /topic/submission/{id} after each test case and at final verdict
public record VerdictUpdate(Long submissionId, Integer testIndex, String status, String finalVerdict) {}
