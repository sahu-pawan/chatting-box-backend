package com.chattingbox.controller;

import com.chattingbox.model.ChatMessage;
import com.chattingbox.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository messageRepository;

    /**
     * Client sends to: /app/chat.send/{roomId}
     * Server broadcasts to: /topic/chat.{roomId}
     */
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage incoming) {
        // sanitize / set server-side fields
        incoming.setRoomId(roomId);
        incoming.setTimestamp(LocalDateTime.now());
        if (incoming.getType() == null)
            incoming.setType("CHAT");

        // persist
        messageRepository.save(incoming);

        // broadcast to subscribers of /topic/chat.{roomId}
        messagingTemplate.convertAndSend("/topic/chat." + roomId, incoming);

        // for video call signaling
        if ("SIGNAL".equals(incoming.getType())) {
            messagingTemplate.convertAndSend("/topic/signal." + roomId, incoming);
        }
        // for voice call singaling
        if ("VOICE_SIGNAL".equals(incoming.getType())) {
            messagingTemplate.convertAndSend("/topic/voice_signal." + roomId, incoming);
        }
    }
}
