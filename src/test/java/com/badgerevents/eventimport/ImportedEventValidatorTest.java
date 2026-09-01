package com.badgerevents.eventimport;

import com.badgerevents.event.EventSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportedEventValidatorTest {

    private final ImportedEventValidator validator =
            new ImportedEventValidator();

    @Test
    void rejectsMissingTitle() {
        ImportedEventData data =
                validData(null, null);

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(
                        EventImportValidationException.class
                )
                .hasMessage("title is required");
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        Instant startTime =
                Instant.parse("2026-09-01T15:00:00Z");

        ImportedEventData data = validData(
                "Career Fair",
                startTime.minusSeconds(60)
        );

        assertThatThrownBy(() -> validator.validate(data))
                .isInstanceOf(
                        EventImportValidationException.class
                )
                .hasMessage(
                        "endTime must not be before startTime"
                );
    }

    private ImportedEventData validData(
            String title,
            Instant endTime
    ) {
        return new ImportedEventData(
                EventSource.UW_EVENTS,
                "222983",
                title,
                "Description",
                "CAREER",
                Instant.parse("2026-09-01T15:00:00Z"),
                endTime,
                "Union South",
                "https://example.edu/career-fair"
        );
    }
}