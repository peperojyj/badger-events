package com.badgerevents.eventimport;

import com.badgerevents.external.uw.UwEventMapper;
import com.badgerevents.external.uw.UwEventResponse;
import com.badgerevents.external.uw.UwEventsClient;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;

@Service
public class EventImportService {

    private final UwEventsClient uwEventsClient;
    private final UwEventMapper uwEventMapper;
    private final EventImportRecordProcessor recordProcessor;
    private final ImportRunRepository importRunRepository;
    private final ImportFailureRepository importFailureRepository;

    public EventImportService(
            UwEventsClient uwEventsClient,
            UwEventMapper uwEventMapper,
            EventImportRecordProcessor recordProcessor,
            ImportRunRepository importRunRepository,
            ImportFailureRepository importFailureRepository
    ) {
        this.uwEventsClient = uwEventsClient;
        this.uwEventMapper = uwEventMapper;
        this.recordProcessor = recordProcessor;
        this.importRunRepository = importRunRepository;
        this.importFailureRepository = importFailureRepository;
    }

    public EventImportResult importUpcomingEvents() {
        ImportRun run =
                importRunRepository.save(ImportRun.start());

        List<UwEventResponse> responses;

        try {
            responses =
                    uwEventsClient.fetchUpcomingEvents();
        } catch (RuntimeException exception) {
            run.fail(messageOf(exception));
            importRunRepository.save(run);

            return EventImportResult.from(run);
        }

        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        try {
            for (UwEventResponse response : responses) {
                try {
                    ImportedEventData data =
                            uwEventMapper
                                    .toImportedEventData(response);

                    ImportAction action =
                            recordProcessor.process(data);

                    switch (action) {
                        case CREATED -> createdCount++;
                        case UPDATED -> updatedCount++;
                        case SKIPPED -> skippedCount++;
                    }
                } catch (DateTimeException exception) {
                    failedCount++;

                    saveFailure(
                            run,
                            response,
                            ImportFailureType.MAPPING,
                            exception
                    );
                } catch (
                        EventImportValidationException exception
                ) {
                    failedCount++;

                    saveFailure(
                            run,
                            response,
                            ImportFailureType.VALIDATION,
                            exception
                    );
                } catch (DataAccessException exception) {
                    failedCount++;

                    saveFailure(
                            run,
                            response,
                            ImportFailureType.PERSISTENCE,
                            exception
                    );
                }
            }
        } catch (RuntimeException exception) {
            run.fail(messageOf(exception));
            importRunRepository.save(run);

            throw exception;
        }

        run.complete(
                createdCount,
                updatedCount,
                skippedCount,
                failedCount
        );

        importRunRepository.save(run);

        return EventImportResult.from(run);
    }

    private void saveFailure(
            ImportRun run,
            UwEventResponse response,
            ImportFailureType failureType,
            RuntimeException exception
    ) {
        String externalId =
                response == null || response.id() == null
                        ? null
                        : response.id().toString();

        ImportFailure failure = ImportFailure.create(
                run.getId(),
                externalId,
                failureType,
                messageOf(exception)
        );

        importFailureRepository.save(failure);
    }

    private String messageOf(RuntimeException exception) {
        if (
                exception.getMessage() == null
                || exception.getMessage().isBlank()
        ) {
            return exception.getClass().getSimpleName();
        }

        return exception.getMessage();
    }
}