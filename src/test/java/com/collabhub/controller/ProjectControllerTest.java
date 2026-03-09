package com.collabhub.controller;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.mapper.ProjectMapperImpl;
import com.collabhub.security.JwtService;
import com.collabhub.security.UserDetailsServiceImpl;
import com.collabhub.service.ProjectService;
import com.collabhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProjectMapperImpl.class)
@DisplayName("ProjectController")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    private User alice;
    private User bob;
    private Project mvp;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", "alice@test.com", "MANAGER","password123!");
        bob = new User("Bob", "bob@test.com", "DEVELOPER","password123!");

        mvp = new Project("CollabHub MVP", "Core platform");
        mvp.setId(UUID.randomUUID());
        mvp.addMember(alice);
        mvp.addMember(bob);

        when(projectService.getById(mvp.getId())).thenReturn(mvp);
    }

    // ── GET /api/projects ─────────────────────────────────────
    @Nested
    @DisplayName("GET /api/projects")
    class GetAllProjects {

        @Test
        @DisplayName("should return 200 with project list")
        void shouldReturn200WithProjects() throws Exception {
            when(projectService.getAllProjects()).thenReturn(List.of(mvp));

            mockMvc.perform(get("/api/projects")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("CollabHub MVP"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$[0].memberCount").value(2));
        }

        @Test
        @DisplayName("should filter by status param")
        void shouldFilterByStatus() throws Exception {
            Project archived = new Project("Old Project", "archived");
            archived.setStatus("ARCHIVED");
            when(projectService.getAllProjects()).thenReturn(List.of(mvp, archived));

            mockMvc.perform(get("/api/projects?status=ACTIVE")).andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].name").value("CollabHub MVP"));
        }
    }

    // ── GET /api/projects/{id} ────────────────────────────────
    @Nested
    @DisplayName("GET /api/projects/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 with project when found")
        void shouldReturn200WhenFound() throws Exception {
            when(projectService.getById(mvp.getId())).thenReturn(mvp);

            mockMvc.perform(get("/api/projects/" + mvp.getId())).andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("CollabHub MVP"));
        }

        @Test
        @DisplayName("should return 404 when project not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            when(projectService.getById(randomId)).thenThrow(new ResourceNotFoundException("Project", randomId));

            mockMvc.perform(get("/api/projects/" + randomId)).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("should return 400 for invalid UUID format")
        void shouldReturn400ForInvalidUUID() throws Exception {
            mockMvc.perform(get("/api/projects/not-a-uuid")).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    // ── POST /api/projects ────────────────────────────────────
    @Nested
    @DisplayName("POST /api/projects")
    class CreateProject {

        @Test
        @DisplayName("should return 201 when project created")
        void shouldReturn201OnSuccess() throws Exception {
            when(userService.getByEmail("alice@collabhub.com")).thenReturn(alice);
            when(projectService.createProject(anyString(), anyString(), any())).thenReturn(mvp);

            mockMvc.perform(post("/api/projects").with(user("alice@collabhub.com").roles("MANAGER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("name", "CollabHub MVP", "description",
                            "Core platform", "creatorEmail", "alice@collabhub.com"))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/projects/")))
                    .andExpect(jsonPath("$.name").value("CollabHub MVP"));
        }

        @Test
        @DisplayName("should return 404 when creator email not found")
        void shouldReturn404WhenCreatorNotFound() throws Exception {
            when(userService.getByEmail(anyString()))
                    .thenThrow(new ResourceNotFoundException("User", "unknown@test.com"));

            mockMvc.perform(post("/api/projects").with(user("alice@collabhub.com").roles("MANAGER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Map.of("name", "Test Project", "description", "desc", "creatorEmail", "unknown@test.com"))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when name is too short")
        void shouldReturn400WhenNameTooShort() throws Exception {
            mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Map.of("name", "AB", "description", "desc", "creatorEmail", "alice@collabhub.com"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[0]", containsString("3 and 100 characters")));
        }

        @Test
        @DisplayName("should return 400 when required fields missing")
        void shouldReturn400WhenFieldsMissing() throws Exception {
            mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(2))));
        }
    }

    // ── DELETE /api/projects/{id} ─────────────────────────────
    @Nested
    @DisplayName("DELETE /api/projects/{id}")
    class DeleteProject {

        @Test
        @DisplayName("should return 204 on successful delete")
        void shouldReturn204OnDelete() throws Exception {
            mockMvc.perform(delete("/api/projects/" + mvp.getId())).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when project not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            org.mockito.Mockito.doThrow(new ResourceNotFoundException("Project", randomId)).when(projectService)
                    .deleteProject(randomId);

            mockMvc.perform(delete("/api/projects/" + randomId)).andExpect(status().isNotFound());
        }
    }
}
