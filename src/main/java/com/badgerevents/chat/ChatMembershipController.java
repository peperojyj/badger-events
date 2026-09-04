package com.badgerevents.chat;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/chat-membership")
public class ChatMembershipController {

    private final ChatMembershipService chatMembershipService;

    public ChatMembershipController(
            ChatMembershipService chatMembershipService
    ) {
        this.chatMembershipService = chatMembershipService;
    }

    @GetMapping
    public ChatMembershipResponse getMembership(
            @PathVariable Long eventId,
            Principal principal
    ) {
        return chatMembershipService.getMembership(
                eventId,
                currentUserEmail(principal)
        );
    }

    @PutMapping
    public ChatMembershipResponse join(
            @PathVariable Long eventId,
            Principal principal
    ) {
        return chatMembershipService.join(
                eventId,
                currentUserEmail(principal)
        );
    }

    @DeleteMapping
    public ChatMembershipResponse leave(
            @PathVariable Long eventId,
            Principal principal
    ) {
        return chatMembershipService.leave(
                eventId,
                currentUserEmail(principal)
        );
    }

    @GetMapping("/members")
    public List<ChatMemberResponse> getMembers(
            @PathVariable Long eventId
    ) {
        return chatMembershipService.getMembers(eventId);
    }

    private String currentUserEmail(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        return principal.getName();
    }
}