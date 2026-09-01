package com.badgerevents.external.uw;

import com.badgerevents.event.EventSource;
import com.badgerevents.eventimport.ImportedEventData;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
public class UwEventMapper {

    public ImportedEventData toImportedEventData(
            UwEventResponse response
    ) {
        return new ImportedEventData(
                EventSource.UW_EVENTS,
                response.id() == null
                        ? null
                        : response.id().toString(),
                normalizeText(response.title()),
                normalizeText(response.description()),
                firstCategory(response.tags()),
                toInstant(response.startDateUnix()),
                toInstant(response.endDateUnix()),
                normalizeText(response.location()),
                normalizeText(response.url())
        );
    }

    private Instant toInstant(Long unixSeconds) {
        return unixSeconds == null
                ? null
                : Instant.ofEpochSecond(unixSeconds);
    }

    private String firstCategory(List<String> tags) {
        if (tags == null) {
            return "OTHER";
        }

        for (String tag : tags) {
            String normalizedTag = normalizeText(tag);

            if (normalizedTag != null) {
                return normalizedTag.toUpperCase(Locale.ROOT);
            }
        }

        return "OTHER";
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}