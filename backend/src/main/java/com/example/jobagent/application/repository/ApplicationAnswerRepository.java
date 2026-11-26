package com.example.jobagent.application.repository;

import com.example.jobagent.application.entity.ApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

    List<ApplicationAnswer> findByApplicationIdOrderByIdAsc(Long applicationId);

    Optional<ApplicationAnswer> findByApplicationIdAndFieldKey(Long applicationId, String fieldKey);
}