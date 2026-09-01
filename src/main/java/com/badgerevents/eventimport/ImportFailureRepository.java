package com.badgerevents.eventimport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportFailureRepository
        extends JpaRepository<ImportFailure, Long> {

    List<ImportFailure> findAllByImportRunIdOrderByIdAsc(
            Long importRunId
    );
}