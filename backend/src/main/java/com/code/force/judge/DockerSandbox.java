  package com.code.force.judge;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DockerSandbox {

    private final DockerClient docker;

    public DockerSandbox(DockerClient docker) {
        this.docker = docker;
    }

    public SandboxResult run(String image, Path workDir, String[] cmd,
                             List<String> envVars, int totalTimeoutMs, long memoryLimitMb) throws Exception {

        Bind bind = new Bind(workDir.toAbsolutePath().toString(), new Volume("/app"));

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(bind)
                .withNetworkMode("none")
                .withMemory(memoryLimitMb * 1024 * 1024)
                .withCpuCount(1L)
                .withPidsLimit(64L);

        CreateContainerResponse container = docker.createContainerCmd(image)
                .withCmd(cmd)
                .withEnv(envVars.toArray(new String[0]))
                .withHostConfig(hostConfig)
                .withWorkingDir("/app")
                .exec();

        String id = container.getId();
        docker.startContainerCmd(id).exec();

        try {
            boolean finished = docker.waitContainerCmd(id)
                    .start()
                    .awaitCompletion(totalTimeoutMs, TimeUnit.MILLISECONDS);

            if (!finished) {
                docker.killContainerCmd(id).exec();
                return new SandboxResult("", true, false);
            }

            boolean oomKilled = Boolean.TRUE.equals(
                    docker.inspectContainerCmd(id).exec().getState().getOOMKilled());

            StringBuilder sb = new StringBuilder();
            docker.logContainerCmd(id)
                    .withStdOut(true)
                    .withStdErr(false)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            sb.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();

            return new SandboxResult(sb.toString().trim(), false, oomKilled);
        } finally {
            docker.removeContainerCmd(id).withForce(true).exec();
        }
    }
}
