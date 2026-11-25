package com.example.jobagent.job.repository;

import com.example.jobagent.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByDedupeKey(String dedupeKey);
    Optional<Job> findBySourceIdAndExternalId(Short sourceId, String externalId);
    Page<Job> findByIsActiveTrueOrderByPostedAtDesc(Pageable pageable);
}