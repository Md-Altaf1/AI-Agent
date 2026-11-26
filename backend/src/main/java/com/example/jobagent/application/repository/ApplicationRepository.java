package com.example.jobagent.application.repository;

import com.example.jobagent.application.entity.Application;
import com.example.jobagent.application.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);

    Page<Application> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Page<Application> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ApplicationStatus status, Pageable pageable);
}