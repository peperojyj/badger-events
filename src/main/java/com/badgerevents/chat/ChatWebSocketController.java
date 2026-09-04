package com.badgerevents.chat;

import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // React가 보내는 데 사용하는 객체가 아님. 서버가 broadcast할 때 사용.

    public ChatWebSocketController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/events/{eventId}/messages")
    public void sendMessage(
            @DestinationVariable Long eventId,
            @Valid @Payload SendChatMessageRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        ChatMessageResponse savedMessage =
                chatService.saveMessage(
                        eventId,
                        principal.getName(),
                        request.content()
                );

        // 저장된 메세지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId,
                savedMessage
        );
    }
}