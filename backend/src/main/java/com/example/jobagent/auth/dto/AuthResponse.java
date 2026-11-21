package com.example.jobagent.auth.dto;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final Long userId;
    private final String email;
    private final String role;

    public AuthResponse(String accessToken, Long userId, String email, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}