package com.chattingbox.controller;

import com.chattingbox.dto.MatchJoinDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Very small in-memory matchmaking controller.
 * Clients send to: /app/match/join
 * Server notifies to: /queue/match.{clientId}
 */
@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final SimpMessagingTemplate messagingTemplate;

    // queue per mode
    private final Map<String, Queue<MatchJoinDTO>> queues = new ConcurrentHashMap<>();

    public void ensureMode(String mode) {
        queues.computeIfAbsent(mode, m -> new ConcurrentLinkedQueue<>());
    }

    @org.springframework.messaging.handler.annotation.MessageMapping("/match/join")
    public void joinQueue(MatchJoinDTO join) {
        if (join == null || join.getClientId() == null)
            return;
        String mode = (join.getMode() == null) ? "video" : join.getMode();

        ensureMode(mode);
        Queue<MatchJoinDTO> q = queues.get(mode);

        q.add(join);

        // attempt to match
        MatchJoinDTO a = q.poll();
        MatchJoinDTO b = q.poll();

        if (a != null && b != null) {
            // generate room id
            String roomId = UUID.randomUUID().toString();

            // notify both clients via per-client topic (no authenticated principal
            // required)
            messagingTemplate.convertAndSend("/topic/match." + a.getClientId(),
                    new MatchResponse(roomId, b.getClientId(), mode));
            messagingTemplate.convertAndSend("/topic/match." + b.getClientId(),
                    new MatchResponse(roomId, a.getClientId(), mode));
        } else {
            // if incomplete, put back the remaining one
            if (a != null)
                q.add(a);
        }
    }

    // simple response DTO (internal class)
    public static class MatchResponse {
        public String roomId;
        public String peerClientId;
        public String mode;

        public MatchResponse(String roomId, String peerClientId, String mode) {
            this.roomId = roomId;
            this.peerClientId = peerClientId;
            this.mode = mode;
        }
    }
}
