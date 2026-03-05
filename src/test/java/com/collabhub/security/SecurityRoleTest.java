package com.collabhub.security;

import com.collabhub.StartupRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRoleTest {

    @Autowired MockMvc mockMvc;
    @MockBean StartupRunner startupRunner;

    @Nested
    class DeleteProject {

        @Test
        @WithMockUser(roles = "DEVELOPER")
        void developerCannotDeleteProject() throws Exception {
            mockMvc.perform(delete("/api/projects/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void managerCanDeleteProject() throws Exception {
            // 404 is fine — it means security passed, just no data
            mockMvc.perform(delete("/api/projects/1"))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    result.getResponse().getStatus() != 403));
        }

        @Test
        void unauthenticatedCannotDeleteProject() throws Exception {
            mockMvc.perform(delete("/api/projects/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateProject {

        @Test
        @WithMockUser(roles = "DEVELOPER")
        void developerCannotCreateProject() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void managerCanCreateProject() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    result.getResponse().getStatus() != 403));
        }

        @Test
        void unauthenticatedCannotCreateProject() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteTask {

        @Test
        @WithMockUser(roles = "DEVELOPER")
        void developerCannotDeleteTask() throws Exception {
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void managerCanDeleteTask() throws Exception {
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    result.getResponse().getStatus() != 403));
        }
    }
}
