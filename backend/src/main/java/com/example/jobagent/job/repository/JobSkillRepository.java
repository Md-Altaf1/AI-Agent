package com.example.jobagent.job.repository;

import com.example.jobagent.job.entity.JobSkill;
import com.example.jobagent.job.entity.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
    List<JobSkill> findByIdJobId(Long jobId);
    void deleteByIdJobId(Long jobId);
}