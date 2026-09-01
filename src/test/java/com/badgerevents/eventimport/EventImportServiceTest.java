package com.badgerevents.eventimport;

import com.badgerevents.external.uw.UwEventMapper;
import com.badgerevents.external.uw.UwEventResponse;
import com.badgerevents.external.uw.UwEventsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventImportServiceTest {

    private UwEventsClient uwEventsClient;
    private UwEventMapper uwEventMapper;
    private EventImportRecordProcessor recordProcessor;
    private ImportRunRepository importRunRepository;
    private ImportFailureRepository importFailureRepository;
    private EventImportService eventImportService;

    @BeforeEach
    void setUp() {
        uwEventsClient =
                mock(UwEventsClient.class);

        uwEventMapper =
                new UwEventMapper();

        recordProcessor =
                mock(EventImportRecordProcessor.class);

        importRunRepository =
                mock(ImportRunRepository.class);

        importFailureRepository =
                mock(ImportFailureRepository.class);

        eventImportService =
                new EventImportService(
                        uwEventsClient,
                        uwEventMapper,
                        recordProcessor,
                        importRunRepository,
                        importFailureRepository
                );

        when(importRunRepository.save(
                any(ImportRun.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );
    }

    @Test
    void countsCreateUpdateAndSkipResults() {
        UwEventResponse first =
                response(1L, "First");

        UwEventResponse second =
                response(2L, "Second");

        UwEventResponse third =
                response(3L, "Third");

        when(uwEventsClient.fetchUpcomingEvents())
                .thenReturn(
                        List.of(first, second, third)
                );

        when(recordProcessor.process(
                any(ImportedEventData.class)
        )).thenReturn(
                ImportAction.CREATED,
                ImportAction.UPDATED,
                ImportAction.SKIPPED
        );

        EventImportResult result =
                eventImportService.importUpcomingEvents();

        assertThat(result.status())
                .isEqualTo(ImportRunStatus.SUCCESS);

        assertThat(result.createdCount())
                .isEqualTo(1);

        assertThat(result.updatedCount())
                .isEqualTo(1);

        assertThat(result.skippedCount())
                .isEqualTo(1);

        assertThat(result.failedCount())
                .isZero();
    }

    @Test
    void recordsInvalidEventAndContinuesImport() {
        UwEventResponse invalid =
                response(1L, null);

        UwEventResponse valid =
                response(2L, "Valid event");

        when(uwEventsClient.fetchUpcomingEvents())
                .thenReturn(
                        List.of(invalid, valid)
                );

        when(recordProcessor.process(
                any(ImportedEventData.class)
        )).thenThrow(
                new EventImportValidationException(
                        "title is required"
                )
        ).thenReturn(
                ImportAction.CREATED
        );

        EventImportResult result =
                eventImportService.importUpcomingEvents();

        assertThat(result.status())
                .isEqualTo(
                        ImportRunStatus.PARTIAL_FAILURE
                );

        assertThat(result.createdCount())
                .isEqualTo(1);

        assertThat(result.failedCount())
                .isEqualTo(1);

        verify(importFailureRepository)
                .save(any(ImportFailure.class));
    }

    @Test
    void marksRunFailedWhenFetchingUwEventsFails() {
        when(uwEventsClient.fetchUpcomingEvents())
                .thenThrow(
                        new RuntimeException(
                                "UW API unavailable"
                        )
                );

        EventImportResult result =
                eventImportService.importUpcomingEvents();

        assertThat(result.status())
                .isEqualTo(ImportRunStatus.FAILED);

        assertThat(result.errorMessage())
                .isEqualTo("UW API unavailable");

        verify(recordProcessor, never())
                .process(any(ImportedEventData.class));
    }

    private UwEventResponse response(
            Long id,
            String title
    ) {
        return new UwEventResponse(
                id,
                title,
                "Description",
                1_788_150_600L,
                null,
                "https://example.edu/" + id,
                List.of("science"),
                "Memorial Union"
        );
    }
}