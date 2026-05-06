package com.code.force.service;

import com.code.force.dto.VerdictUpdate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessenger {

    private final SimpMessagingTemplate messaging;

    public WebSocketMessenger(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    // push a per-test-case result to the frontend
    public void sendTestResult(Long submissionId, int testIndex, String status) {
        messaging.convertAndSend(
                "/topic/submission/" + submissionId,
                new VerdictUpdate(submissionId, testIndex, status, null)
        );
    }

    // push the final overall verdict once all tests are done
    public void sendFinalVerdict(Long submissionId, String finalVerdict) {
        messaging.convertAndSend(
                "/topic/submission/" + submissionId,
                new VerdictUpdate(submissionId, null, null, finalVerdict)
        );
    }
}
