package com.code.force.judge;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Writes runner templates and user code into the submission temp directory.
@Component
public class CodeEnricher {

    @Value("classpath:templates/js_runner_template.js")
    private Resource jsTemplate;

    @Value("classpath:templates/java_runner_template.sh")
    private Resource javaTemplate;

    public void prepareJs(Path workDir, String userCode) throws IOException {
        String runner = jsTemplate.getContentAsString(StandardCharsets.UTF_8);
        Files.writeString(workDir.resolve("solution.js"), runner);
        Files.writeString(workDir.resolve("user_solution.js"), userCode);
    }

    public void prepareJava(Path workDir, String userCode) throws IOException {
        String runner = javaTemplate.getContentAsString(StandardCharsets.UTF_8);
        Files.writeString(workDir.resolve("run_tests.sh"), runner);
        Files.writeString(workDir.resolve("Solution.java"), userCode);
    }
}
