package com.badgerevents.external.uw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class UwEventsClient {

    private final RestClient restClient;
    private final int limit;

    public UwEventsClient (
        RestClient uwEventsRestClient,
        @Value("${badgerevents.uw-events.limit}") int limit
    ) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException(
                "UW Events limit must be between 1 and 100"
            );
        }

        this.restClient = uwEventsRestClient;
        this.limit = limit;
    }


    public List<UwEventResponse> fetchUpcomingEvents() {
        UwEventResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/events.json")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(UwEventResponse[].class);

        if (response == null) {
            return List.of();
        }

        return List.copyOf(Arrays.asList(response));
    }
}