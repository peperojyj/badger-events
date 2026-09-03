package com.badgerevents.event;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventSummaryResponse> getEvents(
            @RequestParam(defaultValue = "") String keyword,
            Authentication authentication
    ) {
        return eventService.getUpcomingEvents(
                keyword,
                getCurrentUserEmail(authentication)
        );
    }

    @GetMapping("/{eventId}")
    public EventDetailResponse getEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return eventService.findPublishedEvent(
                        eventId,
                        getCurrentUserEmail(authentication)
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));
    }

    private String getCurrentUserEmail(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication.getName();
    }
}