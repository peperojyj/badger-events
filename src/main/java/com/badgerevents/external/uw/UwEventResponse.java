package com.badgerevents.external.uw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UwEventResponse(
        Long id,
        String title,
        String description,
        Long startDateUnix,
        Long endDateUnix,
        String url,
        List<String> tags,
        String location
) {
}
