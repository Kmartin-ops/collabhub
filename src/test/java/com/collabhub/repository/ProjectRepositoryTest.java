package com.collabhub.repository;

import com.collabhub.domain.Project;
import com.collabhub.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProjectRepository")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager em;

    private User alice;
    private User bob;
    private Project mvp;
    private Project mobile;

    @BeforeEach
    void setUp() {
        alice  = em.persistAndFlush(new User("Alice", "alice@test.com", "MANAGER"));
        bob    = em.persistAndFlush(new User("Bob",   "bob@test.com",   "DEVELOPER"));

        mvp = new Project("CollabHub MVP", "Core platform");
        mvp.addMember(alice);
        mvp.addMember(bob);
        mvp = em.persistAndFlush(mvp);

        mobile = new Project("CollabHub Mobile", "Mobile app");
        mobile.addMember(alice);
        mobile = em.persistAndFlush(mobile);
    }

    @Nested
    @DisplayName("findByStatus()")
    class FindByStatus {

        @Test
        @DisplayName("should find active projects")
        void shouldFindActiveProjects() {
            List<Project> active = projectRepository.findByStatus("ACTIVE");
            assertEquals(2, active.size());
        }

        @Test
        @DisplayName("should return empty for unused status")
        void shouldReturnEmptyForUnusedStatus() {
            List<Project> archived = projectRepository.findByStatus("ARCHIVED");
            assertTrue(archived.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByIdWithMembers()")
    class FindByIdWithMembers {

        @Test
        @DisplayName("should load project with members")
        void shouldLoadWithMembers() {
            Optional<Project> found =
                    projectRepository.findByIdWithMembers(mvp.getId());

            assertTrue(found.isPresent());
            assertEquals(2, found.get().getMembers().size());
        }

        @Test
        @DisplayName("should return empty for unknown ID")
        void shouldReturnEmptyForUnknownId() {
            Optional<Project> found =
                    projectRepository.findByIdWithMembers(java.util.UUID.randomUUID());
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByMember()")
    class FindByMember {

        @Test
        @DisplayName("should find projects alice is member of")
        void shouldFindAliceProjects() {
            List<Project> aliceProjects = projectRepository.findByMember(alice);
            assertEquals(2, aliceProjects.size());
        }

        @Test
        @DisplayName("should find projects bob is member of")
        void shouldFindBobProjects() {
            List<Project> bobProjects = projectRepository.findByMember(bob);
            assertEquals(1, bobProjects.size());
            assertEquals("CollabHub MVP", bobProjects.get(0).getName());
        }

        @Test
        @DisplayName("should return empty for user with no projects")
        void shouldReturnEmptyForNonMember() {
            User carol = em.persistAndFlush(
                    new User("Carol", "carol@test.com", "DEVELOPER"));
            List<Project> carolProjects = projectRepository.findByMember(carol);
            assertTrue(carolProjects.isEmpty());
        }
    }

    @Nested
    @DisplayName("countByStatus()")
    class CountByStatus {

        @Test
        @DisplayName("should count active projects correctly")
        void shouldCountActiveProjects() {
            long count = projectRepository.countByStatus("ACTIVE");
            assertEquals(2, count);
        }
    }
}