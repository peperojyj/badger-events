package com.badgerevents.interest;

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

import static org.assertj.core.api.Assertions
        .assertThat;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventInterestIntegrationTest {

    private static final String EMAIL =
            "interest@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventInterestRepository
            eventInterestRepository;

    private Event earlierEvent;
    private Event laterEvent;

    @BeforeEach
    void setUp() {
        authService.register(
                new RegisterRequest(
                        EMAIL,
                        "password123",
                        "Interested Student"
                )
        );

        earlierEvent =
                eventRepository.saveAndFlush(
                        event(
                                "interest-earlier",
                                "Earlier Event",
                                "2026-10-10T18:00:00Z"
                        )
                );

        laterEvent =
                eventRepository.saveAndFlush(
                        event(
                                "interest-later",
                                "Later Event",
                                "2026-10-20T18:00:00Z"
                        )
                );
    }

    @Test
    void unauthenticatedUserCannotAddInterest()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/events/{eventId}/interests",
                                earlierEvent.getId()
                        )
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedAddDoesNotCreateDuplicateInterest()
            throws Exception {

        addInterest(laterEvent.getId())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.eventId")
                                .value(laterEvent.getId())
                )
                .andExpect(
                        jsonPath("$.interestedCount")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.interestedByMe")
                                .value(true)
                );

        addInterest(laterEvent.getId())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.interestedCount")
                                .value(1)
                );

        assertThat(
                eventInterestRepository.count()
        ).isEqualTo(1);
    }

    @Test
    void removeDeletesOnlyTheInterestRelationship()
            throws Exception {

        addInterest(earlierEvent.getId())
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete(
                                "/api/events/{eventId}/interests",
                                earlierEvent.getId()
                        )
                                .with(
                                        user(EMAIL)
                                                .roles("USER")
                                )
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.interestedCount")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.interestedByMe")
                                .value(false)
                );

        assertThat(
                eventInterestRepository.count()
        ).isZero();

        assertThat(
                eventRepository.existsById(
                        earlierEvent.getId()
                )
        ).isTrue();
    }

    @Test
    void listShowsPublicCountAndPrioritizesMyInterestedEvent()
            throws Exception {

        addInterest(laterEvent.getId())
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/events")
                                .with(
                                        user(EMAIL)
                                                .roles("USER")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(laterEvent.getId())
                )
                .andExpect(
                        jsonPath(
                                "$[0].interestedCount"
                        ).value(1)
                )
                .andExpect(
                        jsonPath(
                                "$[0].interestedByMe"
                        ).value(true)
                );

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(earlierEvent.getId())
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(laterEvent.getId())
                )
                .andExpect(
                        jsonPath(
                                "$[1].interestedCount"
                        ).value(1)
                )
                .andExpect(
                        jsonPath(
                                "$[1].interestedByMe"
                        ).value(false)
                );
    }

    @Test
    void nonexistentEventCannotBeInterested()
            throws Exception {

        addInterest(999_999L)
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet
            .ResultActions addInterest(Long eventId)
            throws Exception {

        return mockMvc.perform(
                post(
                        "/api/events/{eventId}/interests",
                        eventId
                )
                        .with(
                                user(EMAIL)
                                        .roles("USER")
                        )
                        .with(csrf())
        );
    }

    private Event event(
            String externalId,
            String title,
            String startTime
    ) {
        return Event.createImported(
                EventSource.UW_EVENTS,
                externalId,
                title,
                "Event description",
                "ACADEMIC",
                Instant.parse(startTime),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        );
    }
}