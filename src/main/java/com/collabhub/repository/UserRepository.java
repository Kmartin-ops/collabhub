package com.collabhub.repository;

import com.collabhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Derived — SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Derived — SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // Derived — SELECT * FROM users WHERE role = ?
    java.util.List<User> findByRole(String role);
}