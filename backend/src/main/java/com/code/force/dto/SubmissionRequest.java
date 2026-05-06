package com.code.force.dto;

public record SubmissionRequest(String problemSlug, String language, String code, Long contestId) {}
