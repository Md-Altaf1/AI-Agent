package com.example.jobagent.auth.service;

import com.example.jobagent.auth.dto.AuthResponse;
import com.example.jobagent.auth.dto.LoginRequest;
import com.example.jobagent.auth.dto.RegisterRequest;
import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.auth.security.JwtService;
import com.example.jobagent.common.exception.EmailAlreadyExistsException;
import com.example.jobagent.common.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(normalizedEmail, hashedPassword);
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Find user by email
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        // 2. Verify BCrypt password
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 3. Generate JWT token
        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}