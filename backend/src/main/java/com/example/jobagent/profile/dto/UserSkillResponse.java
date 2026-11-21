package com.example.jobagent.profile.dto;

import com.example.jobagent.profile.entity.SkillProficiency;
import com.example.jobagent.profile.entity.SkillSource;
import com.example.jobagent.profile.entity.UserSkill;

import java.time.Instant;

public class UserSkillResponse {

    private final Long id;
    private final String skillName;
    private final SkillProficiency proficiency;
    private final SkillSource source;
    private final Instant createdAt;

    public UserSkillResponse(
            Long id,
            String skillName,
            SkillProficiency proficiency,
            SkillSource source,
            Instant createdAt) {
        this.id = id;
        this.skillName = skillName;
        this.proficiency = proficiency;
        this.source = source;
        this.createdAt = createdAt;
    }

    public static UserSkillResponse fromEntity(UserSkill skill) {
        return new UserSkillResponse(
                skill.getId(),
                skill.getSkillName(),
                skill.getProficiency(),
                skill.getSource(),
                skill.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public String getSkillName() { return skillName; }
    public SkillProficiency getProficiency() { return proficiency; }
    public SkillSource getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
}