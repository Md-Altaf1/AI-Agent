package com.example.jobagent.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_skills")
public class JobSkill {

    @EmbeddedId
    private JobSkillId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("jobId")
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired = true;

    protected JobSkill() {
    }

    public JobSkill(Job job, String skillName, boolean isRequired) {
        this.job = job;
        this.id = new JobSkillId(job.getId(), skillName);
        this.isRequired = isRequired;
    }

    public JobSkillId getId() { return id; }
    public Job getJob() { return job; }
    public String getSkillName() { return id.getSkillName(); }
    public boolean isRequired() { return isRequired; }
    public void setRequired(boolean required) { isRequired = required; }
}