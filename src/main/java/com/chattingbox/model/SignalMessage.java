package com.chattingbox.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {
    private String type; // CHAT, OFFER, ANSWER, CANDIDATE, REJECT
    private String from;
    private String content; // for chat
    private String sdp; // for offer/answer
    private Object candidate; // for ICE
}
