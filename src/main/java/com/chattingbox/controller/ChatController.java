package com.chattingbox.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chattingbox.model.ChatMessage;
import com.chattingbox.repository.ChatMessageRepository;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatMessageRepository repo;

    public ChatController(ChatMessageRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestBody ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        if (message.getRoomId() == null || message.getRoomId().isEmpty()) {
            // roomId bana do sender+receiver ka combo se
            message.setRoomId(generateRoomId(message.getSender(), message.getReceiver()));
        }
        return repo.save(message);
    }

    @GetMapping("/messages/{roomId}")
    public List<ChatMessage> getMessages(@PathVariable String roomId) {
        return repo.findByRoomIdOrderByTimestampAsc(roomId);
    }

    private String generateRoomId(String sender, String receiver) {
        // fix order so both users generate same roomId
        return (sender.compareTo(receiver) < 0)
                ? sender + "_" + receiver
                : receiver + "_" + sender;
    }

}
