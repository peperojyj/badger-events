package com.badgerevents.chat;

import com.badgerevents.auth.AuthService;
import com.badgerevents.auth.RegisterRequest;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatMembershipIntegrationTest {

    private static final String EMAIL = "member@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ChatMembershipRepository chatMembershipRepository;

    private User userEntity;
    private Event event;

    @BeforeEach
    void setUp() {
        authService.register(new RegisterRequest(
                EMAIL,
                "password123",
                "Conversation Member"
        ));
        userEntity = userRepository.findByEmail(EMAIL).orElseThrow();

        event = eventRepository.saveAndFlush(Event.createImported(
                EventSource.UW_EVENTS,
                "membership-event",
                "Membership Event",
                "Description",
                "SOCIAL",
                Instant.parse("2026-10-10T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        ));
    }

    @Test
    void reportsNotJoinedBeforeTheFirstJoin() throws Exception {
        mockMvc.perform(get(
                        "/api/events/{eventId}/chat-membership",
                        event.getId()
                ).with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joined").value(false))
                .andExpect(jsonPath("$.memberCount").value(0));
    }

    @Test
    void joiningTwiceKeepsOneMembership() throws Exception {
        mockMvc.perform(put(
                        "/api/events/{eventId}/chat-membership",
                        event.getId()
                ).with(user(EMAIL).roles("USER")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joined").value(true))
                .andExpect(jsonPath("$.memberCount").value(1));

        mockMvc.perform(put(
                        "/api/events/{eventId}/chat-membership",
                        event.getId()
                ).with(user(EMAIL).roles("USER")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(1));

        org.assertj.core.api.Assertions.assertThat(
                chatMembershipRepository.countByUserIdAndEventId(
                        userEntity.getId(),
                        event.getId()
                )
        ).isEqualTo(1);
    }

    @Test
    void listsMembersWithoutExposingTheirEmail() throws Exception {
        mockMvc.perform(put(
                "/api/events/{eventId}/chat-membership",
                event.getId()
        ).with(user(EMAIL).roles("USER")).with(csrf()));

        mockMvc.perform(get(
                        "/api/events/{eventId}/chat-membership/members",
                        event.getId()
                ).with(user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId")
                        .value(userEntity.getId()))
                .andExpect(jsonPath("$[0].displayName")
                        .value("Conversation Member"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    void leavingRemovesTheMembership() throws Exception {
        mockMvc.perform(put(
                "/api/events/{eventId}/chat-membership",
                event.getId()
        ).with(user(EMAIL).roles("USER")).with(csrf()));

        mockMvc.perform(delete(
                        "/api/events/{eventId}/chat-membership",
                        event.getId()
                ).with(user(EMAIL).roles("USER")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joined").value(false))
                .andExpect(jsonPath("$.memberCount").value(0));
    }

    @Test
    void anonymousUserCannotJoin() throws Exception {
        mockMvc.perform(put(
                        "/api/events/{eventId}/chat-membership",
                        event.getId()
                ).with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
