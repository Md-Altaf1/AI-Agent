package com.example.jobagent.matching.repository;

import com.example.jobagent.matching.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    Optional<JobMatch> findByResumeVersionIdAndJobId(Long resumeVersionId, Long jobId);

    List<JobMatch> findByResumeVersionIdOrderByMatchScoreDesc(Long resumeVersionId);
}