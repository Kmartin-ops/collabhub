package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.registry.UserRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private final UserRegistry registry = new UserRegistry();

    public User createUser(String name, String email, String role) {
        if (registry.exists(email)) {
            throw new IllegalArgumentException(
                    "User with email '" + email + "' already exists");
        }
        User user = new User(name, email, role);
        registry.save(user);
        System.out.println("[UserService] Created user: " + name + " (" + email + ")");
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return registry.findByEmail(email);
    }

    public Collection<User> findAll() {
        return registry.findAll();
    }

    public boolean exists(String email) {
        return registry.exists(email);
    }
}