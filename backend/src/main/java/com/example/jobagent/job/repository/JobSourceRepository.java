package com.example.jobagent.job.repository;

import com.example.jobagent.job.entity.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSourceRepository extends JpaRepository<JobSource, Short> {
    Optional<JobSource> findByCode(String code);
    List<JobSource> findByIsEnabledTrueOrderByDisplayNameAsc();
}