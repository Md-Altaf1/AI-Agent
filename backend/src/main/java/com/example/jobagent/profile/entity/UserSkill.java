package com.example.jobagent.profile.entity;

import com.example.jobagent.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkillProficiency proficiency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillSource source = SkillSource.SELF_DECLARED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserSkill() {
    }

    public UserSkill(User user, String skillName, SkillProficiency proficiency, SkillSource source) {
        this.user = user;
        this.skillName = skillName;
        this.proficiency = proficiency;
        this.source = source;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSkillName() { return skillName; }
    public SkillProficiency getProficiency() { return proficiency; }
    public SkillSource getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }

    public void setProficiency(SkillProficiency proficiency) { this.proficiency = proficiency; }
    public void setSource(SkillSource source) { this.source = source; }
}