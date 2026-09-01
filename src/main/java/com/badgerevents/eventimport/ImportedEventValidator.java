package com.badgerevents.eventimport;

import org.springframework.stereotype.Component;

@Component
public class ImportedEventValidator {

    public void validate(ImportedEventData data) {
        if (data.source() == null) {
            throw new EventImportValidationException(
                    "source is required"
            );
        }

        requireText(
                data.externalId(),
                "externalId",
                255
        );

        requireText(
                data.title(),
                "title",
                255
        );

        if (data.startTime() == null) {
            throw new EventImportValidationException(
                    "startTime is required"
            );
        }

        if (
                data.endTime() != null
                && data.endTime().isBefore(data.startTime())
        ) {
            throw new EventImportValidationException(
                    "endTime must not be before startTime"
            );
        }

        checkLength(
                data.category(),
                "category",
                100
        );

        checkLength(
                data.location(),
                "location",
                255
        );

        checkLength(
                data.eventUrl(),
                "eventUrl",
                1000
        );
    }

    private void requireText(
            String value,
            String fieldName,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new EventImportValidationException(
                    fieldName + " is required"
            );
        }

        checkLength(
                value,
                fieldName,
                maxLength
        );
    }

    private void checkLength(
            String value,
            String fieldName,
            int maxLength
    ) {
        if (
                value != null
                && value.length() > maxLength
        ) {
            throw new EventImportValidationException(
                    fieldName
                            + " must be at most "
                            + maxLength
                            + " characters"
            );
        }
    }
}