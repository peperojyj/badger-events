package com.badgerevents.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void usesPrincipalAsAuthorAndBroadcastsAfterSaveReturns() {
        ChatWebSocketController controller = new ChatWebSocketController(
                chatService,
                messagingTemplate
        );
        Principal principal = () -> "chat@example.com";
        SendChatMessageRequest request = new SendChatMessageRequest("Hello");
        ChatMessageResponse savedMessage = new ChatMessageResponse(
                91L,
                36L,
                7L,
                "Chat Student",
                "Hello",
                Instant.parse("2026-09-03T10:30:00Z")
        );

        when(chatService.saveMessage(
                36L,
                "chat@example.com",
                "Hello"
        )).thenReturn(savedMessage);

        controller.sendMessage(36L, request, principal);

        InOrder order = inOrder(chatService, messagingTemplate);
        order.verify(chatService).saveMessage(
                36L,
                "chat@example.com",
                "Hello"
        );
        order.verify(messagingTemplate).convertAndSend(
                "/topic/events/36",
                savedMessage
        );
    }

    @Test
    void rejectsMessageWithoutPrincipal() {
        ChatWebSocketController controller = new ChatWebSocketController(
                chatService,
                messagingTemplate
        );

        assertThatThrownBy(() -> controller.sendMessage(
                36L,
                new SendChatMessageRequest("Hello"),
                null
        )).isInstanceOf(AccessDeniedException.class);

        verify(chatService, never()).saveMessage(
                36L,
                null,
                "Hello"
        );
    }
}
