package com.code.force.config;

import com.code.force.model.Difficulty;
import com.code.force.model.Problem;
import com.code.force.repository.ProblemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

// Runs once on startup — scans problems dir and inserts any missing problems into DB.
// Add a new folder with metadata.json and it appears automatically on next restart.
@Component
public class DataSeeder implements ApplicationRunner {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;

    @Value("${judge.problems-dir}")
    private String problemsDir;

    public DataSeeder(ProblemRepository problemRepository, ObjectMapper objectMapper) {
        this.problemRepository = problemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File root = new File(problemsDir);
        if (!root.exists() || !root.isDirectory()) {
            System.out.println("DataSeeder: problems dir not found at " + problemsDir);
            return;
        }

        for (File problemDir : root.listFiles(File::isDirectory)) {
            File metadataFile = new File(problemDir, "metadata.json");
            if (!metadataFile.exists()) continue;

            JsonNode meta = objectMapper.readTree(metadataFile);
            String slug = meta.get("problem_slug").asText();

            if (problemRepository.findBySlug(slug).isPresent()) {
                System.out.println("DataSeeder: skipping " + slug + " (already exists)");
                continue;
            }

            Problem problem = new Problem();
            problem.setSlug(slug);
            problem.setTitle(meta.get("title").asText());
            problem.setDifficulty(Difficulty.valueOf(meta.get("difficulty").asText()));
            problemRepository.save(problem);

            System.out.println("DataSeeder: inserted problem → " + slug);
        }
    }
}
