package com.chattingbox.controller;

import com.chattingbox.dto.SignalMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SignalController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Clients send to: /app/signal/{roomId}
     * Server relays to: /topic/signal.{roomId}
     */
    @MessageMapping("/signal/{roomId}")
    public void relaySignal(@DestinationVariable String roomId, SignalMessageDTO dto) {
        if (dto == null || dto.getKind() == null) {
            return;
        }

        // Relay to all subscribers of the room
        messagingTemplate.convertAndSend("/topic/signal." + roomId, dto);
    }
}
