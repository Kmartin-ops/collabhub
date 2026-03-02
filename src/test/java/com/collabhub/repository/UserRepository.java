package com.collabhub.repository;

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
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = em.persistAndFlush(
                new User("Alice", "alice@test.com", "MANAGER"));
        bob = em.persistAndFlush(
                new User("Bob", "bob@test.com", "DEVELOPER"));
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("should find existing user by email")
        void shouldFindByEmail() {
            Optional<User> found = userRepository.findByEmail("alice@test.com");
            assertTrue(found.isPresent());
            assertEquals("Alice", found.get().getName());
        }

        @Test
        @DisplayName("should return empty for unknown email")
        void shouldReturnEmptyForUnknown() {
            Optional<User> found = userRepository.findByEmail("unknown@test.com");
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true for existing email")
        void shouldReturnTrueForExisting() {
            assertTrue(userRepository.existsByEmail("alice@test.com"));
        }

        @Test
        @DisplayName("should return false for unknown email")
        void shouldReturnFalseForUnknown() {
            assertFalse(userRepository.existsByEmail("nobody@test.com"));
        }
    }

    @Nested
    @DisplayName("findByRole()")
    class FindByRole {

        @Test
        @DisplayName("should return users with matching role")
        void shouldFindByRole() {
            List<User> developers = userRepository.findByRole("DEVELOPER");
            assertEquals(1, developers.size());
            assertEquals("Bob", developers.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list for unused role")
        void shouldReturnEmptyForUnusedRole() {
            List<User> admins = userRepository.findByRole("ADMIN");
            assertTrue(admins.isEmpty());
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist user with generated UUID")
        void shouldPersistWithId() {
            User carol = userRepository.save(
                    new User("Carol", "carol@test.com", "DEVELOPER"));
            assertNotNull(carol.getId());
        }

        @Test
        @DisplayName("should persist createdAt timestamp")
        void shouldPersistCreatedAt() {
            User carol = userRepository.save(
                    new User("Carol", "carol@test.com", "DEVELOPER"));
            assertNotNull(carol.getCreatedAt());
        }

        @Test
        @DisplayName("should find all saved users")
        void shouldFindAll() {
            List<User> all = userRepository.findAll();
            assertEquals(2, all.size()); // alice + bob from setUp
        }
    }
}