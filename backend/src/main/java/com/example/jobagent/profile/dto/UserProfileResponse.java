package com.example.jobagent.profile.dto;

import com.example.jobagent.profile.entity.UserProfile;

import java.math.BigDecimal;
import java.time.Instant;

public class UserProfileResponse {

    private final Long userId;
    private final String email;
    private final String fullName;
    private final String phone;
    private final String city;
    private final String highestEducation;
    private final Integer graduationYear;
    private final BigDecimal totalExperienceYears;
    private final String linkedinUrl;
    private final String githubUrl;
    private final Instant updatedAt;

    public UserProfileResponse(
            Long userId,
            String email,
            String fullName,
            String phone,
            String city,
            String highestEducation,
            Integer graduationYear,
            BigDecimal totalExperienceYears,
            String linkedinUrl,
            String githubUrl,
            Instant updatedAt) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.city = city;
        this.highestEducation = highestEducation;
        this.graduationYear = graduationYear;
        this.totalExperienceYears = totalExperienceYears;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.updatedAt = updatedAt;
    }

    public static UserProfileResponse fromEntity(UserProfile profile, String email) {
        return new UserProfileResponse(
                profile.getUserId(),
                email,
                profile.getFullName(),
                profile.getPhone(),
                profile.getCity(),
                profile.getHighestEducation(),
                profile.getGraduationYear(),
                profile.getTotalExperienceYears(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getUpdatedAt()
        );
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getHighestEducation() { return highestEducation; }
    public Integer getGraduationYear() { return graduationYear; }
    public BigDecimal getTotalExperienceYears() { return totalExperienceYears; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getGithubUrl() { return githubUrl; }
    public Instant getUpdatedAt() { return updatedAt; }
}