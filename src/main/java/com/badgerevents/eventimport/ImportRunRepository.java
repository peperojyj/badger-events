package com.badgerevents.eventimport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportRunRepository
        extends JpaRepository<ImportRun, Long> {

    List<ImportRun> findAllByOrderByStartedAtDesc();
}