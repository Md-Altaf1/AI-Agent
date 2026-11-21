package com.example.jobagent.profile.entity;

import com.example.jobagent.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String city;

    @Column(name = "highest_education", length = 100)
    private String highestEducation;

    @Column(name = "graduation_year", columnDefinition = "SMALLINT UNSIGNED")
    private Integer graduationYear;

    @Column(name = "total_experience_years", nullable = false, precision = 3, scale = 1)
    private BigDecimal totalExperienceYears = BigDecimal.ZERO;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(User user, String fullName) {
        this.user = user;
        this.fullName = fullName;
    }

    @PrePersist
    @PreUpdate
    void onSave() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public User getUser() { return user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getHighestEducation() { return highestEducation; }
    public void setHighestEducation(String highestEducation) { this.highestEducation = highestEducation; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public BigDecimal getTotalExperienceYears() { return totalExperienceYears; }
    public void setTotalExperienceYears(BigDecimal totalExperienceYears) { this.totalExperienceYears = totalExperienceYears; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public Instant getUpdatedAt() { return updatedAt; }
}