package com.collabhub.security;

import com.collabhub.domain.User;
import com.collabhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService      jwtService;
    private final UserRepository  userRepository;
    private final ObjectMapper    objectMapper;

    public OAuth2SuccessHandler(JwtService jwtService,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.jwtService     = jwtService;
        this.userRepository = userRepository;
        this.objectMapper   = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OAuth2 user not found: " + email));

        // Reuse your existing JwtService to generate a token
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        // Return JWT as JSON — frontend reads this
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of(
                        "accessToken", token,
                        "email", email,
                        "name", user.getName(),
                        "role", user.getRole()
                ))
        );
    }
}
