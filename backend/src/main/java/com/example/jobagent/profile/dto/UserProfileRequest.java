package com.example.jobagent.profile.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;


public class UserProfileRequest {


    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Highest education must not exceed 100 characters")
    private String highestEducation;

    @Min(value = 1950, message = "Graduation year must be >= 1950")
    @Max(value = 2100, message = "Graduation year must be <= 2100")
    private Integer graduationYear;

    // field ke upar
    @Digits(integer = 2, fraction = 1, message = "Experience must have at most 2 digits and 1 decimal place")
    @DecimalMin(value = "0.0", message = "Experience cannot be negative")
    @DecimalMax(value = "60.0", message = "Experience must be <= 60 years")
    private BigDecimal totalExperienceYears = BigDecimal.ZERO;

    @Size(max = 255)
    private String linkedinUrl;

    @Size(max = 255)
    private String githubUrl;

    // Getters and Setters
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
}