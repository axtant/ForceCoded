package com.code.force.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Reads problem content from disk — statement, metadata, and test cases.
// DB only stores slug/title/difficulty. Everything else lives in the filesystem.
@Service
public class ProblemFileService {

    @Value("${judge.problems-dir}")
    private String problemsDir;

    private final ObjectMapper objectMapper;

    public ProblemFileService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Returns the raw markdown string for the problem statement page
    public String loadStatement(String slug) throws IOException {
        Path file = Path.of(problemsDir, slug, "statement.md");
        return Files.readString(file);
    }

    // Returns parsed metadata (limits, test counts)
    public JsonNode loadMetadata(String slug) throws IOException {
        Path file = Path.of(problemsDir, slug, "metadata.json");
        return objectMapper.readTree(file.toFile());
    }

    // Returns only sample test cases (visible to users on the problem page)
    public List<SampleTest> loadSampleTests(String slug) throws IOException {
        JsonNode meta = loadMetadata(slug);
        int sampleCount = meta.get("sample_count").asInt();

        List<SampleTest> samples = new ArrayList<>();
        for (int i = 1; i <= sampleCount; i++) {
            Path testDir = Path.of(problemsDir, slug, "test_" + i);
            String input    = Files.readString(testDir.resolve("input.txt")).trim();
            String output   = Files.readString(testDir.resolve("output.txt")).trim();
            samples.add(new SampleTest(i, input, output));
        }
        return samples;
    }

    // Copies all test cases (sample + hidden) into destDir for Docker bundling
    public void copyTestCasesToDir(String slug, Path destDir) throws IOException {
        JsonNode meta = loadMetadata(slug);
        int totalTests = meta.get("total_tests").asInt();

        Files.createDirectories(destDir);
        for (int i = 1; i <= totalTests; i++) {
            Path src  = Path.of(problemsDir, slug, "test_" + i);
            Path dest = destDir.resolve("test_" + i);
            Files.createDirectories(dest);
            Files.copy(src.resolve("input.txt"),  dest.resolve("input.txt"));
            Files.copy(src.resolve("output.txt"), dest.resolve("output.txt"));
        }
    }

    public record SampleTest(int index, String input, String expectedOutput) {}
}
