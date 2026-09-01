package com.badgerevents.eventimport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/imports")
public class EventImportAdminController {

    private final EventImportService eventImportService;
    private final EventImportQueryService queryService;

    public EventImportAdminController(
            EventImportService eventImportService,
            EventImportQueryService queryService
    ) {
        this.eventImportService = eventImportService;
        this.queryService = queryService;
    }

    @PostMapping("/events")
    public ResponseEntity<EventImportResult> importEvents() {
        EventImportResult result =
                eventImportService.importUpcomingEvents();

        HttpStatus status =
                result.status() == ImportRunStatus.FAILED
                        ? HttpStatus.BAD_GATEWAY
                        : HttpStatus.OK;

        return ResponseEntity
                .status(status)
                .body(result);
    }

    @GetMapping
    public List<ImportRunSummaryResponse> getRuns() {
        return queryService.getRuns();
    }

    @GetMapping("/{runId}")
    public ImportRunDetailResponse getRun(
            @PathVariable Long runId
    ) {
        return queryService.getRun(runId);
    }
}