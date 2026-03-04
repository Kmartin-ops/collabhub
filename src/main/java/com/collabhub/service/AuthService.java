package com.collabhub.service;

import com.collabhub.domain.RefreshToken;
import com.collabhub.domain.User;
import com.collabhub.dto.*;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.repository.UserRepository;
import com.collabhub.security.JwtService;
import com.collabhub.security.RefreshTokenService;
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
    private final RefreshTokenService   refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authManager,
                       RefreshTokenService refreshTokenService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
        this.authManager     = authManager;
        this.refreshTokenService =refreshTokenService;
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

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("User registered: email={}", user.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken(),
                user.getEmail(), user.getRole(), user.getName());
    }
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt: email={}", request.email());

        // Throws BadCredentialsException if wrong password
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        RefreshToken refreshToken =refreshTokenService.createRefreshToken(user);
        log.info("User logged in: email={}", user.getEmail());

        return new AuthResponse(accessToken,refreshToken.getToken(),
                user.getEmail(), user.getRole(), user.getName());
    }
    @Transactional
    public AuthResponse refresh(RefreshRequest request){
        RefreshToken refreshToken=
                refreshTokenService.validateAndGet(request.refreshToken());
        User user     = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        //Rotate - revoke old, issue new refresh token
        RefreshToken newRefresh = refreshTokenService.createRefreshToken(user);
        log.info("Token refreshed for user={}", user.getEmail());
        return new AuthResponse(accessToken, newRefresh.getToken(),
                user.getEmail(), user.getRole(), user.getName());

    }
    @Transactional
    public void logout(String email){
        User user = userRepository.findByEmail(email).orElseThrow();
        refreshTokenService.revokeAllForUser(user);
        log.info("User logged our: email={}",email);
    }
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request){
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(
                request.currentPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Revoke all fresh tokens - force re-login everywhere
        refreshTokenService.revokeAllForUser(user);
        log.info("Password changed for user={}",email);

    }
}