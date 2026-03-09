package com.collabhub.controller;

import com.collabhub.domain.Project;
import com.collabhub.domain.Task;
import com.collabhub.domain.User;
import com.collabhub.mapper.TaskMapperImpl;
import com.collabhub.security.JwtService;
import com.collabhub.security.UserDetailsServiceImpl;
import com.collabhub.service.ProjectService;
import com.collabhub.service.TaskService;
import com.collabhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TaskMapperImpl.class)
@DisplayName("TaskController")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    private User manager;
    private User assignee;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        manager = new User("Alice", "alice@collabhub.com", "MANAGER","password123!");
        manager.setPasswordHash("hashed");

        assignee = new User("Bob", "bob@collabhub.com", "DEVELOPER","password123!");
        assignee.setPasswordHash("hashed");

        project = new Project("CollabHub MVP", "Core platform");
        project.setId(UUID.randomUUID());

        task = new Task("Build login", "HIGH", LocalDate.now().plusDays(5), project);
        task.setId(UUID.randomUUID());
        task.setAssignee(assignee);

        when(taskService.getById(task.getId())).thenReturn(task);
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class SearchTasks {

        @Test
        @DisplayName("should return paged task results")
        void shouldReturnPagedResults() throws Exception {
            when(taskService.search(any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(task)));

            mockMvc.perform(get("/api/tasks").param("keyword", "login")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title").value("Build login"))
                    .andExpect(jsonPath("$.totalELements").value(1));
        }
    }

    @Nested
    @DisplayName("GET endpoints")
    class GetEndpoints {

        @Test
        @DisplayName("should get task by id")
        void shouldGetById() throws Exception {
            mockMvc.perform(get("/api/tasks/" + task.getId())).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(task.getId().toString()))
                    .andExpect(jsonPath("$.title").value("Build login"));
        }

        @Test
        @DisplayName("should get overdue tasks")
        void shouldGetOverdue() throws Exception {
            when(taskService.findOverdue()).thenReturn(List.of(task));

            mockMvc.perform(get("/api/tasks/overdue")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Build login"));
        }

        @Test
        @DisplayName("should get project board tasks")
        void shouldGetBoard() throws Exception {
            when(taskService.findKanbanBoard(project.getId())).thenReturn(List.of(task));

            mockMvc.perform(get("/api/tasks/board").param("projectId", project.getId().toString()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].projectName").value("CollabHub MVP"));
        }
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        @DisplayName("should return 201 when task is created")
        void shouldCreateTask() throws Exception {
            Task created = new Task("Write docs", "MEDIUM", LocalDate.now().plusDays(7), project);
            created.setId(UUID.randomUUID());

            when(projectService.getById(project.getId())).thenReturn(project);
            when(userService.getByEmail(manager.getEmail())).thenReturn(manager);
            when(taskService.createTask("Write docs", "MEDIUM", created.getDueDate(), project, manager))
                    .thenReturn(created);

            mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("title", "Write docs", "priority", "MEDIUM",
                            "dueDate", created.getDueDate().toString(), "projectId", project.getId().toString(),
                            "creatorEmail", manager.getEmail()))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/tasks/" + created.getId()))
                    .andExpect(jsonPath("$.title").value("Write docs"))
                    .andExpect(jsonPath("$.projectName").value("CollabHub MVP"));
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{id}")
    class UpdateTask {

        @Test
        @DisplayName("should update task fields, assignee and status")
        void shouldUpdateTask() throws Exception {
            User newAssignee = new User("Carol", "carol@collabhub.com", "DEVELOPER","password123!");
            newAssignee.setPasswordHash("hashed");
            when(userService.getByEmail("carol@collabhub.com")).thenReturn(newAssignee);

            mockMvc.perform(put("/api/tasks/" + task.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("title", "Refine login", "priority", "LOW",
                            "dueDate", LocalDate.now().plusDays(10).toString(), "assigneeEmail", "carol@collabhub.com",
                            "status", "IN_PROGRESS"))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Refine login"))
                    .andExpect(jsonPath("$.priority").value("LOW"));

            verify(taskService).assignTask(task, newAssignee, newAssignee);
            verify(taskService).changeStatus(task, "IN_PROGRESS", assignee);
        }

        @Test
        @DisplayName("should fallback to first user when task has no assignee")
        void shouldUseFallbackActorWhenUnassigned() throws Exception {
            Task unassigned = new Task("Unassigned", "HIGH", LocalDate.now().plusDays(4), project);
            UUID taskId = UUID.randomUUID();
            unassigned.setId(taskId);
            when(taskService.getById(taskId)).thenReturn(unassigned);
            when(userService.findAll()).thenReturn(List.of(manager));

            mockMvc.perform(put("/api/tasks/" + taskId).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("status", "DONE")))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Unassigned"));

            verify(taskService).changeStatus(unassigned, "DONE", manager);
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {

        @Test
        @DisplayName("should return 204 on successful delete")
        void shouldDeleteTask() throws Exception {
            mockMvc.perform(delete("/api/tasks/" + task.getId())).andExpect(status().isNoContent());

            verify(taskService).deleteTask(task.getId());
        }
    }
}
