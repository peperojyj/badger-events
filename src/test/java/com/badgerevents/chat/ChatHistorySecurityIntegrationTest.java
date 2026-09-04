package com.badgerevents.chat;

import com.badgerevents.auth.AuthService;
import com.badgerevents.auth.RegisterRequest;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatHistorySecurityIntegrationTest {

    private static final String EMAIL = "history@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ChatMembershipService chatMembershipService;

    private Event event;

    @BeforeEach
    void setUp() {
        authService.register(new RegisterRequest(
                EMAIL,
                "password123",
                "History Student"
        ));

        event = eventRepository.saveAndFlush(Event.createImported(
                EventSource.UW_EVENTS,
                "history-security-event",
                "History Security Event",
                "Description",
                "SOCIAL",
                Instant.parse("2026-10-10T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        ));
    }

    @Test
    void anonymousUserCannotReadChatHistory() throws Exception {
        mockMvc.perform(get(
                        "/api/events/{eventId}/messages",
                        event.getId()
                ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedNonMemberCannotReadChatHistory() throws Exception {
        mockMvc.perform(get(
                        "/api/events/{eventId}/messages",
                        event.getId()
                ).with(user(EMAIL).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinedUserCanReadChatHistory() throws Exception {
        chatMembershipService.join(event.getId(), EMAIL);

        mockMvc.perform(get(
                        "/api/events/{eventId}/messages",
                        event.getId()
                ).with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
