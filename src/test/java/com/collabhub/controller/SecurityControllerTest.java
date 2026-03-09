package com.collabhub.controller;

import com.collabhub.config.SecurityConfig;
import com.collabhub.dto.ProjectResponse;
import com.collabhub.mapper.ProjectMapperImpl;
import com.collabhub.security.JwtAuthFilter;
import com.collabhub.security.TaskAuthService;
import com.collabhub.security.UserDetailsServiceImpl;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import({ ProjectMapperImpl.class, SecurityConfig.class, JwtAuthFilter.class, UserDetailsServiceImpl.class })
@DisplayName("Security — Role-Based Access Control")
class SecurityControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProjectService projectService;
    @MockBean
    UserService userService;
    @MockBean
    TaskService taskService;
    @MockBean
    TaskAuthService taskAuthService;
    @MockBean
    com.collabhub.security.JwtService jwtService;
    @MockBean
    com.collabhub.repository.UserRepository userRepository;
    @MockBean
    com.collabhub.security.OAuth2UserServiceImpl oauth2UserService;
    @MockBean
    com.collabhub.security.OAuth2SuccessHandler oauth2SuccessHandler;
    private static final UUID PROJECT_ID = UUID.randomUUID();

    @Nested
    @DisplayName("GET /api/projects — any authenticated user")
    class GetProjects {

        @Test
        @WithMockUser(username = "alice@test.com", roles = "MANAGER")
        @DisplayName("MANAGER can list projects")
        void managerCanListProjects() throws Exception {
            when(projectService.getAllProjects()).thenReturn(List.of());
            mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "bob@test.com", roles = "DEVELOPER")
        @DisplayName("DEVELOPER can list projects")
        void developerCanListProjects() throws Exception {
            when(projectService.getAllProjects()).thenReturn(List.of());
            mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated user gets 401")
        void unauthenticatedGets401() throws Exception {
            mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/projects — MANAGER only")
    class CreateProject {

        @Test
        @WithMockUser(username = "alice@test.com", roles = "MANAGER")
        @DisplayName("MANAGER can create project")
        void managerCanCreate() throws Exception {
            var project = new com.collabhub.domain.Project("Test", "desc");
            project.setId(PROJECT_ID);
            when(userService.getByEmail(any()))
                    .thenReturn(new com.collabhub.domain.User("Alice", "alice@test.com", "MANAGER","password123!"));
            when(projectService.createProject(any(), any(), any())).thenReturn(project);

            mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                    {"name": "Test Project", "description": "desc"}
                    """)).andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = "bob@test.com", roles = "DEVELOPER")
        @DisplayName("DEVELOPER gets 403 on create")
        void developerCannotCreate() throws Exception {
            mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                    {"name": "Test Project", "description": "desc"}
                    """)).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/projects/{id} — MANAGER only")
    class DeleteProject {

        @Test
        @WithMockUser(username = "alice@test.com", roles = "MANAGER")
        @DisplayName("MANAGER can delete project")
        void managerCanDelete() throws Exception {
            doNothing().when(projectService).deleteProject(PROJECT_ID);
            mockMvc.perform(delete("/api/projects/" + PROJECT_ID)).andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "bob@test.com", roles = "DEVELOPER")
        @DisplayName("DEVELOPER gets 403 on delete")
        void developerCannotDelete() throws Exception {
            mockMvc.perform(delete("/api/projects/" + PROJECT_ID)).andExpect(status().isForbidden());
        }
    }
}
