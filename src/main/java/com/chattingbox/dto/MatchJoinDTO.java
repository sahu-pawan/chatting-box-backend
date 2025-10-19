package com.chattingbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Client -> server for joining matchmaking queue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchJoinDTO {
    private String clientId; // unique id for the connecting client (client-generated)
    private String username; // optional
    private String mode; // "video" | "voice" | "chat"
}
