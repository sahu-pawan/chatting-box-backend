package com.chattingbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for WebRTC signaling.
 *
 * kind: "offer" | "answer" | "candidate" | "hangup"
 * payload: stringified SDP or ICE JSON
 * fromUserId/fromUsername: sender identity (optional, useful for client)
 * roomId: which room/peer group this signal belongs to
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignalMessageDTO {
    private String roomId;
    private String fromUser; // username or id of sender
    private String kind; // offer | answer | candidate | hangup
    private String payload; // sdp or ice candidate JSON string
}
