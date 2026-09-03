package com.badgerevents.interest;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/interests")
public class EventInterestController {

    private final EventInterestService eventInterestService;

    public EventInterestController(
            EventInterestService eventInterestService
    ) {
        this.eventInterestService = eventInterestService;
    }

    @PostMapping
    public EventInterestResponse addInterest(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return eventInterestService.addInterest(
                eventId,
                authentication.getName()
        );
    }

    @DeleteMapping
    public EventInterestResponse removeInterest(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return eventInterestService.removeInterest(
                eventId,
                authentication.getName()
        );
    }
}