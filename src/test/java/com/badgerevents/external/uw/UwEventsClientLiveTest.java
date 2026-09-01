package com.badgerevents.external.uw;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UwEventsClientLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(
            named = "RUN_LIVE_UW_TEST",
            matches = "true"
    )
    void fetchesEventsFromTheLiveUwApi() {
        UwEventsClient client = new UwEventsClient(
                RestClient.builder()
                        .baseUrl("https://today.wisc.edu")
                        .build(),
                3
        );

        List<UwEventResponse> events =
                client.fetchUpcomingEvents();

        assertThat(events).isNotEmpty();

        events.forEach(event ->
                System.out.printf(
                        "UW event %s: %s%n",
                        event.id(),
                        event.title()
                )
        );
    }
}