package com.example.jobagent.profile.controller;

import com.example.jobagent.profile.dto.UserProfileRequest;
import com.example.jobagent.profile.dto.UserProfileResponse;
import com.example.jobagent.profile.dto.UserSkillRequest;
import com.example.jobagent.profile.dto.UserSkillResponse;
import com.example.jobagent.profile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.jobagent.profile.dto.UserPreferenceResponse;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(profileService.upsertProfile(authentication.getName(), request));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<UserSkillResponse>> getMySkills(Authentication authentication) {
        return ResponseEntity.ok(profileService.getSkills(authentication.getName()));
    }

    @PostMapping("/skills")
    public ResponseEntity<UserSkillResponse> addSkill(
            Authentication authentication,
            @Valid @RequestBody UserSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addOrUpdateSkill(authentication.getName(), request));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            Authentication authentication,
            @PathVariable Long skillId) {
        profileService.deleteSkill(authentication.getName(), skillId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> getMyPreferences(Authentication authentication) {
        return ResponseEntity.ok(profileService.getPreferences(authentication.getName()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> updateMyPreferences(
            Authentication authentication,
            @Valid @RequestBody com.example.jobagent.profile.dto.UserPreferenceRequest request) {
        return ResponseEntity.ok(profileService.upsertPreferences(authentication.getName(), request));
    }
}