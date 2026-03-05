package com.collabhub.controller;

import com.collabhub.dto.*;
import com.collabhub.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean com.collabhub.security.JwtService jwtService;
    @MockBean com.collabhub.security.UserDetailsServiceImpl userDetailsService;
    @MockBean com.collabhub.security.JwtAuthFilter jwtAuthFilter;

    private static final AuthResponse FAKE_RESPONSE = new AuthResponse(
            "access-token-123",
            "refresh-token-456",
            "john@example.com",
            "DEVELOPER",
            "John Doe"
    );

    @Nested
    class Register {

        @Test
        void shouldReturn200WithTokenOnValidRegistration() throws Exception {
            when(authService.register(any())).thenReturn(FAKE_RESPONSE);

            RegisterRequest request = new RegisterRequest(
                    "John Doe", "john@example.com", "Password1!", "DEVELOPER");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("access-token-123"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.role").value("DEVELOPER"));
        }

        @Test
        void shouldReturn400WhenEmailMissing() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "John Doe", "", "Password1!", "DEVELOPER");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Login {

        @Test
        void shouldReturn200WithTokenOnValidLogin() throws Exception {
            when(authService.login(any())).thenReturn(FAKE_RESPONSE);

            LoginRequest request = new LoginRequest("john@example.com", "Password1!");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        void shouldReturn500OnBadCredentials() throws Exception {
            when(authService.login(any()))
                    .thenThrow(new org.springframework.security.authentication
                            .BadCredentialsException("Bad credentials"));

            LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    class Refresh {

        @Test
        void shouldReturn200WithNewAccessToken() throws Exception {
            when(authService.refresh(any())).thenReturn(FAKE_RESPONSE);

            RefreshRequest request = new RefreshRequest("refresh-token-456");

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("access-token-123"));
        }
    }

    @Nested
    class Logout {

        @Test
        @WithMockUser(username = "john@example.com")
        void shouldReturn204OnLogout() throws Exception {
            doNothing().when(authService).logout(any());

            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class ChangePassword {

        @Test
        @WithMockUser(username = "john@example.com")
        void shouldReturn204OnSuccessfulPasswordChange() throws Exception {
            doNothing().when(authService).changePassword(any(), any());

            ChangePasswordRequest request =
                    new ChangePasswordRequest("OldPass1!", "NewPass1!");

            mockMvc.perform(post("/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }
}
