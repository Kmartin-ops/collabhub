package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.dto.AuthResponse;
import com.collabhub.dto.LoginRequest;
import com.collabhub.dto.RegisterRequest;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.repository.UserRepository;
import com.collabhub.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authManager) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
        this.authManager     = authManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Register attempt: email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", request.email());
        }

        User user = new User(request.name(), request.email(),
                request.role() != null ? request.role() : "DEVELOPER");
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        log.info("User registered: email={} role={}", user.getEmail(), user.getRole());

        return new AuthResponse(token, user.getEmail(),
                user.getRole(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt: email={}", request.email());

        // Throws BadCredentialsException if wrong password
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        log.info("User logged in: email={}", user.getEmail());

        return new AuthResponse(token, user.getEmail(),
                user.getRole(), user.getName());
    }
}