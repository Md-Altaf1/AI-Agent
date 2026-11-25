package com.example.jobagent.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JobSkillId implements Serializable {

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "skill_name", length = 100)
    private String skillName;

    protected JobSkillId() {
    }

    public JobSkillId(Long jobId, String skillName) {
        this.jobId = jobId;
        this.skillName = skillName;
    }

    public Long getJobId() { return jobId; }
    public String getSkillName() { return skillName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobSkillId that)) return false;
        return Objects.equals(jobId, that.jobId) &&
                Objects.equals(skillName, that.skillName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, skillName);
    }
}