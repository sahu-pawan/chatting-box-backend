package com.chattingbox.controller;

import com.chattingbox.model.ChatMessage;
import com.chattingbox.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatMessageRepository repo;
    private final SimpMessagingTemplate messagingTemplate; // ✅ WebSocket ke liye

    // ✅ WebSocket se message bhejna
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage msg) {
        if (msg.getRoomId() == null || msg.getRoomId().isEmpty()) {
            msg.setRoomId(generateRoomId(msg.getSender(), msg.getReceiver()));
        }
        msg.setTimestamp(LocalDateTime.now());
        if (msg.getType() == null)
            msg.setType("CHAT");

        repo.save(msg);

        // WebSocket ke through client ko bhejo
        messagingTemplate.convertAndSend("/topic/messages/" + msg.getRoomId(), msg);
    }

    // ✅ REST se message save karna (optional)
    @PostMapping("/send/{roomId}")
    public ChatMessage sendRest(@PathVariable String roomId, @RequestBody ChatMessage msg) {
        msg.setRoomId(roomId);
        msg.setTimestamp(LocalDateTime.now());
        if (msg.getType() == null)
            msg.setType("CHAT");
        ChatMessage saved = repo.save(msg);

        // REST call ke baad bhi WebSocket clients ko notify karo
        messagingTemplate.convertAndSend("/topic/messages/" + roomId, saved);

        return saved;
    }

    // ✅ Room ke purane messages nikalna
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(repo.findByRoomIdOrderByTimestampAsc(roomId));
    }

    // ✅ RoomId generate karna
    private String generateRoomId(String sender, String receiver) {
        return (sender.compareTo(receiver) < 0)
                ? sender + "_" + receiver
                : receiver + "_" + sender;
    }
}
