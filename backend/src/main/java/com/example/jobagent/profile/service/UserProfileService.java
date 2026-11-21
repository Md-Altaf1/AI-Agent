package com.example.jobagent.profile.service;

import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.common.config.exception.ResourceNotFoundException;
import com.example.jobagent.profile.dto.UserPreferenceRequest;
import com.example.jobagent.profile.dto.UserPreferenceResponse;
import com.example.jobagent.profile.dto.UserProfileRequest;
import com.example.jobagent.profile.dto.UserProfileResponse;
import com.example.jobagent.profile.dto.UserSkillRequest;
import com.example.jobagent.profile.dto.UserSkillResponse;
import com.example.jobagent.profile.entity.SkillSource;
import com.example.jobagent.profile.entity.UserPreference;
import com.example.jobagent.profile.entity.UserProfile;
import com.example.jobagent.profile.entity.UserSkill;
import com.example.jobagent.profile.repository.UserPreferenceRepository;
import com.example.jobagent.profile.repository.UserProfileRepository;
import com.example.jobagent.profile.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserSkillRepository skillRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserProfileService(
            UserProfileRepository profileRepository,
            UserSkillRepository skillRepository,
            UserPreferenceRepository preferenceRepository,
            UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    // ==================== 1. PROFILE METHODS ====================

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);

        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + email));

        return UserProfileResponse.fromEntity(profile, user.getEmail());
    }

    @Transactional
    public UserProfileResponse upsertProfile(String email, UserProfileRequest request) {
        User user = findUserByEmail(email);

        UserProfile profile = profileRepository.findById(user.getId())
                .orElseGet(() -> new UserProfile(user, request.getFullName().trim()));

        profile.setFullName(request.getFullName().trim());
        profile.setPhone(trimToNull(request.getPhone()));
        profile.setCity(trimToNull(request.getCity()));
        profile.setHighestEducation(trimToNull(request.getHighestEducation()));
        profile.setGraduationYear(request.getGraduationYear());
        profile.setTotalExperienceYears(
                request.getTotalExperienceYears() != null ? request.getTotalExperienceYears() : BigDecimal.ZERO
        );
        profile.setLinkedinUrl(trimToNull(request.getLinkedinUrl()));
        profile.setGithubUrl(trimToNull(request.getGithubUrl()));

        UserProfile saved = profileRepository.save(profile);
        return UserProfileResponse.fromEntity(saved, user.getEmail());
    }

    // ==================== 2. SKILLS METHODS ====================

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getSkills(String email) {
        User user = findUserByEmail(email);
        return skillRepository.findByUserIdOrderBySkillNameAsc(user.getId())
                .stream()
                .map(UserSkillResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserSkillResponse addOrUpdateSkill(String email, UserSkillRequest request) {
        User user = findUserByEmail(email);
        String cleanSkillName = request.getSkillName().trim();

        UserSkill skill = skillRepository.findByUserIdAndSkillNameIgnoreCase(user.getId(), cleanSkillName)
                .map(existing -> {
                    existing.setProficiency(request.getProficiency());
                    existing.setSource(SkillSource.SELF_DECLARED);
                    return existing;
                })
                .orElseGet(() -> new UserSkill(
                        user,
                        cleanSkillName,
                        request.getProficiency(),
                        SkillSource.SELF_DECLARED
                ));

        return UserSkillResponse.fromEntity(skillRepository.save(skill));
    }

    @Transactional
    public void deleteSkill(String email, Long skillId) {
        User user = findUserByEmail(email);
        UserSkill skill = skillRepository.findByIdAndUserId(skillId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        skillRepository.delete(skill);
    }

    // ==================== 3. PREFERENCES METHODS ====================

    @Transactional
    public UserPreferenceResponse getPreferences(String email) {
        User user = findUserByEmail(email);
        UserPreference pref = preferenceRepository.findById(user.getId())
                .orElseGet(() -> preferenceRepository.save(new UserPreference(user)));
        return UserPreferenceResponse.fromEntity(pref);
    }

    @Transactional
    public UserPreferenceResponse upsertPreferences(String email, UserPreferenceRequest request) {
        User user = findUserByEmail(email);
        UserPreference pref = preferenceRepository.findById(user.getId())
                .orElseGet(() -> new UserPreference(user));

        pref.setTargetTitles(request.getTargetTitles());
        pref.setPreferredLocations(request.getPreferredLocations());
        pref.setEmploymentTypes(request.getEmploymentTypes());
        pref.setExcludedTitleKeywords(request.getExcludedTitleKeywords());
        pref.setMaxRequiredExperienceYears(request.getMaxRequiredExperienceYears());
        pref.setMinMatchScore(request.getMinMatchScore().byteValue());
        pref.setMaxJobsPerSearch(request.getMaxJobsPerSearch().shortValue());
        pref.setExternalAiEnabled(request.isExternalAiEnabled());

        return UserPreferenceResponse.fromEntity(preferenceRepository.save(pref));
    }

    // ==================== HELPERS ====================

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}