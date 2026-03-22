package com.dojangkok.backend.mq.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class ChatNotificationDto {

    private String type;
    private String messageId;
    private String roomId;
    private String senderId;
    private String senderNickname;
    private String senderProfileImageUrl;
    private Long targetMemberId;
    private String contentType;
    private String preview;
    private Instant createdAt;
}
