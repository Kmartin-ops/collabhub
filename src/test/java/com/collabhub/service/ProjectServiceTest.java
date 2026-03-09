/*
package com.collabhub;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.repository.ProjectRepository;
import com.collabhub.service.ProjectService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectService")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectService projectService;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        // save() returns whatever is passed to it
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        projectService = new ProjectService(projectRepository);

        alice = new User("Alice", "alice@test.com", "MANAGER","password123!");
        bob = new User("Bob", "bob@test.com", "DEVELOPER","password123!");
    }

    @Nested
    @DisplayName("createProject()")
    class CreateProject {

        @Test
        @DisplayName("should create project with ACTIVE status")
        void shouldCreateWithActiveStatus() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertEquals("ACTIVE", project.getStatus());
        }

        @Test
        @DisplayName("should automatically add creator as member")
        void shouldAddCreatorAsMember() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertTrue(project.getMembers().contains(alice));
        }

        @Test
        @DisplayName("should assign a createdAt timestamp")
        void shouldAssignCreatedAt() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertNotNull(project.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("addMember()")
    class AddMember {

        @Test
        @DisplayName("should add new member to project")
        void shouldAddMember() {
            Project project = projectService.createProject("Test", "desc", alice);
            projectService.addMember(project, bob);
            assertTrue(project.getMembers().contains(bob));
        }

        @Test
        @DisplayName("should silently reject duplicate members")
        void shouldRejectDuplicateMembers() {
            Project project = projectService.createProject("Test", "desc", alice);
            projectService.addMember(project, bob);
            projectService.addMember(project, bob); // duplicate
            assertEquals(2, project.getMembers().size()); // alice + bob only
        }
    }

    @Nested
    @DisplayName("getAllProjects()")
    class GetAllProjects {

        @Test
        @DisplayName("should return all projects from repository")
        void shouldReturnAllProjects() {
            Project p1 = new Project("Project 1", "desc");
            Project p2 = new Project("Project 2", "desc");
            when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

            assertEquals(2, projectService.getAllProjects().size());
        }

        @Test
        @DisplayName("should return empty list when no projects")
        void shouldReturnEmptyList() {
            when(projectRepository.findAll()).thenReturn(List.of());
            assertTrue(projectService.getAllProjects().isEmpty());
        }
    }

    @Nested
    @DisplayName("findByMember()")
    class FindByMember {

        @Test
        @DisplayName("should return projects user belongs to")
        void shouldReturnMemberProjects() {
            Project p1 = new Project("Project 1", "desc");
            p1.addMember(alice);
            p1.addMember(bob);

            when(projectRepository.findByMember(bob)).thenReturn(List.of(p1));

            assertEquals(1, projectService.findByMember(bob).size());
        }

        @Test
        @DisplayName("should return empty list for user with no projects")
        void shouldReturnEmptyForNonMember() {
            when(projectRepository.findByMember(bob)).thenReturn(List.of());
            assertTrue(projectService.findByMember(bob).isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteProject()")
    class DeleteProject {

        @Test
        @DisplayName("should delete existing project")
        void shouldDeleteProject() {
            Project project = new Project("Test", "desc");
            when(projectRepository.findByIdWithMembers(project.getId())).thenReturn(Optional.of(project));

            projectService.deleteProject(project.getId());

            verify(projectRepository, times(1)).delete(project);
        }
    }
}
*/
package com.collabhub.service;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.ProjectRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectService")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectService projectService;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        // Mock save() to assign UUID if null, simulating DB
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
            }
            return p;
        });

        projectService = new ProjectService(projectRepository);

        alice = new User("Alice", "alice@test.com", "MANAGER","password123!");
        bob = new User("Bob", "bob@test.com", "DEVELOPER","password123!");
    }

    @Nested
    @DisplayName("createProject()")
    class CreateProject {

        @Test
        @DisplayName("should create project with ACTIVE status")
        void shouldCreateWithActiveStatus() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertEquals("ACTIVE", project.getStatus());
        }

        @Test
        @DisplayName("should automatically add creator as member")
        void shouldAddCreatorAsMember() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertTrue(project.getMembers().contains(alice));
        }

        @Test
        @DisplayName("should assign a createdAt timestamp")
        void shouldAssignCreatedAt() {
            Project project = projectService.createProject("Test", "desc", alice);
            assertNotNull(project.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("addMember()")
    class AddMember {

        @Test
        @DisplayName("should add new member to project")
        void shouldAddMember() {
            Project project = projectService.createProject("Test", "desc", alice);

            // Reset previous interactions to avoid double counting save()
            clearInvocations(projectRepository);

            // Mock findByIdWithMembers to return project
            when(projectRepository.findByIdWithMembers(project.getId()))
                    .thenReturn(Optional.of(project));

            projectService.addMember(project, bob);

            assertTrue(project.getMembers().contains(bob));
            verify(projectRepository, times(1)).save(project);
        }

        @Test
        @DisplayName("should silently reject duplicate members")
        void shouldRejectDuplicateMembers() {
            Project project = projectService.createProject("Test", "desc", alice);

            clearInvocations(projectRepository);

            when(projectRepository.findByIdWithMembers(project.getId()))
                    .thenReturn(Optional.of(project));

            projectService.addMember(project, bob);
            projectService.addMember(project, bob); // duplicate

            assertEquals(2, project.getMembers().size()); // alice + bob only
            verify(projectRepository, times(1)).save(project); // only saved once
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException if project not found")
        void shouldThrowIfProjectNotFound() {
            Project project = new Project("Test", "desc");
            project.setId(UUID.randomUUID());

            when(projectRepository.findByIdWithMembers(project.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> projectService.addMember(project, bob));
        }
    }

    @Nested
    @DisplayName("getAllProjects()")
    class GetAllProjects {

        @Test
        @DisplayName("should return all projects from repository")
        void shouldReturnAllProjects() {
            Project p1 = new Project("Project 1", "desc");
            Project p2 = new Project("Project 2", "desc");
            when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

            assertEquals(2, projectService.getAllProjects().size());
        }

        @Test
        @DisplayName("should return empty list when no projects")
        void shouldReturnEmptyList() {
            when(projectRepository.findAll()).thenReturn(List.of());
            assertTrue(projectService.getAllProjects().isEmpty());
        }
    }

    @Nested
    @DisplayName("findByMember()")
    class FindByMember {

        @Test
        @DisplayName("should return projects user belongs to")
        void shouldReturnMemberProjects() {
            Project p1 = new Project("Project 1", "desc");
            p1.addMember(alice);
            p1.addMember(bob);

            when(projectRepository.findByMember(bob)).thenReturn(List.of(p1));

            assertEquals(1, projectService.findByMember(bob).size());
        }

        @Test
        @DisplayName("should return empty list for user with no projects")
        void shouldReturnEmptyForNonMember() {
            when(projectRepository.findByMember(bob)).thenReturn(List.of());
            assertTrue(projectService.findByMember(bob).isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteProject()")
    class DeleteProject {

        @Test
        @DisplayName("should delete existing project")
        void shouldDeleteProject() {
            Project project = new Project("Test", "desc");
            project.setId(UUID.randomUUID());

            when(projectRepository.findByIdWithMembers(project.getId()))
                    .thenReturn(Optional.of(project));

            projectService.deleteProject(project.getId());

            verify(projectRepository, times(1)).delete(project);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException if project not found")
        void shouldThrowIfProjectNotFound() {
            UUID randomId = UUID.randomUUID();
            when(projectRepository.findByIdWithMembers(randomId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> projectService.deleteProject(randomId));
        }
    }
}