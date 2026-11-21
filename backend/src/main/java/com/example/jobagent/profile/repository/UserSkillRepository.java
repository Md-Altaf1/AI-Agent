package com.example.jobagent.profile.repository;

import com.example.jobagent.profile.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserIdOrderBySkillNameAsc(Long userId);

    Optional<UserSkill> findByUserIdAndSkillNameIgnoreCase(Long userId, String skillName);

    Optional<UserSkill> findByIdAndUserId(Long id, Long userId);
}