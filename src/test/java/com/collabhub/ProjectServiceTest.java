package com.collabhub;

import com.collabhub.domain.*;
import com.collabhub.service.ProjectService;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService")
class ProjectServiceTest {

    private ProjectService projectService;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        alice = new User("Alice", "alice@test.com", "MANAGER");
        bob   = new User("Bob",   "bob@test.com",   "DEVELOPER");
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
        @DisplayName("should assign a unique ID")
        void shouldAssignUniqueId() {
            Project p1 = projectService.createProject("Project 1", "desc", alice);
            Project p2 = projectService.createProject("Project 2", "desc", alice);
            assertNotEquals(p1.getId(), p2.getId());
        }

        @Test
        @DisplayName("should store project in registry")
        void shouldStoreInRegistry() {
            projectService.createProject("Project A", "desc", alice);
            projectService.createProject("Project B", "desc", alice);

            Collection<Project> all = projectService.getAllProjects();
            assertEquals(2, all.size());
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
    @DisplayName("findByMember()")
    class FindByMember {

        @Test
        @DisplayName("should return projects user belongs to")
        void shouldReturnMemberProjects() {
            Project p1 = projectService.createProject("Project 1", "desc", alice);
            Project p2 = projectService.createProject("Project 2", "desc", alice);
            projectService.addMember(p1, bob);
            // bob not added to p2

            assertEquals(1, projectService.findByMember(bob).size());
        }

        @Test
        @DisplayName("should return empty list for user with no projects")
        void shouldReturnEmptyForNonMember() {
            projectService.createProject("Project 1", "desc", alice);
            assertTrue(projectService.findByMember(bob).isEmpty());
        }
    }
}