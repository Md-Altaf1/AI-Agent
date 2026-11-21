package com.example.jobagent.profile.dto;

import com.example.jobagent.profile.entity.SkillProficiency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserSkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String skillName;

    private SkillProficiency proficiency = SkillProficiency.INTERMEDIATE;

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public SkillProficiency getProficiency() { return proficiency; }
    public void setProficiency(SkillProficiency proficiency) { this.proficiency = proficiency; }
}