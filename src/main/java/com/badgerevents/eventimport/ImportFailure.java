package com.badgerevents.eventimport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "import_failures")
public class ImportFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_run_id", nullable = false)
    private Long importRunId;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "failure_type",
            nullable = false,
            length = 30
    )
    private ImportFailureType failureType;

    @Column(
            name = "failure_message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String failureMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected ImportFailure() {
    }

    private ImportFailure(
            Long importRunId,
            String externalId,
            ImportFailureType failureType,
            String failureMessage
    ) {
        this.importRunId = importRunId;
        this.externalId = externalId;
        this.failureType = Objects.requireNonNull(failureType);
        this.failureMessage = Objects.requireNonNull(failureMessage);
        this.createdAt = Instant.now();
    }

    public static ImportFailure create(
            Long importRunId,
            String externalId,
            ImportFailureType failureType,
            String failureMessage
    ) {
        return new ImportFailure(
                importRunId,
                externalId,
                failureType,
                failureMessage
        );
    }

    public Long getId() {
        return id;
    }

    public Long getImportRunId() {
        return importRunId;
    }

    public String getExternalId() {
        return externalId;
    }

    public ImportFailureType getFailureType() {
        return failureType;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}