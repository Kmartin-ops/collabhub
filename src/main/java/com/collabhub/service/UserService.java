package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.registry.UserRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserService {

    private final UserRegistry registry = new UserRegistry();

    public User createUser(String name, String email, String role) {
        if (registry.exists(email)) {
            throw new DuplicateResourceException("User", email);
        }
        User user = new User(name, email, role);
        registry.save(user);
        System.out.println("[UserService] Created: " + name + " (" + email + ")");
        return user;
    }

    public User getByEmail(String email) {
        return registry.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    public Collection<User> findAll() {
        return registry.findAll();
    }

    public boolean exists(String email) {
        return registry.exists(email);
    }
}