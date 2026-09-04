package com.badgerevents.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/messages")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatMessageResponse> getRecentMessages(
            @PathVariable Long eventId,
            Principal principal
    ) {
        return chatService.getRecentMessages(
                eventId,
                principal.getName()
        );
    }
}