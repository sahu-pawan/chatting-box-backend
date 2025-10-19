package com.chattingbox.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String roomId;
    private String sender;
    private String receiver;
    private String content;
    private String type; // CHAT / JOIN / LEAVE
    private LocalDateTime timestamp;
}
