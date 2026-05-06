package com.code.force.judge;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class JudgeEngine {

    private final DockerSandbox sandbox;

    public JudgeEngine(DockerSandbox sandbox) {
        this.sandbox = sandbox;
    }

    public SandboxResult execute(Path submissionDir, String language,
                                 int testCount, int timeLimitMs, int memoryLimitMb) throws Exception {
        String image;
        String[] cmd;
        int totalTimeoutMs;

        if ("JAVASCRIPT".equals(language)) {
            image = "node:18-slim";
            cmd = new String[]{"node", "/app/solution.js"};
            totalTimeoutMs = timeLimitMs * testCount + 5000;
        } else {
            // extra 15s for javac compilation + JVM startup
            image = "eclipse-temurin:21-jdk-alpine";
            cmd = new String[]{"sh", "/app/run_tests.sh"};
            totalTimeoutMs = timeLimitMs * testCount + 15000;
        }

        List<String> env = List.of(
                "TEST_COUNT=" + testCount,
                "TIME_LIMIT_MS=" + timeLimitMs,
                "TIME_LIMIT_SEC=" + Math.max(1, timeLimitMs / 1000)
        );

        return sandbox.run(image, submissionDir, cmd, env, totalTimeoutMs, memoryLimitMb);
    }
}
