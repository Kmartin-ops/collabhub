package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.registry.UserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRegistry registry = new UserRegistry();

    public User createUser(String name, String email, String role) {
        log.debug("Creating user email={} role={}", email, role);
        if (registry.exists(email)) {
            log.warn("Duplicate user attempt: email={}", email);
            throw new DuplicateResourceException("User", email);
        }
        User user = new User(name, email, role);
        registry.save(user);
        log.info("User created: name='{}' email={} role={}", name, email, role);
        return user;
    }

    public User getByEmail(String email) {
        log.debug("Fetching user email={}", email);
        return registry.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: email={}", email);
                    return new ResourceNotFoundException("User", email);
                });
    }

    public Collection<User> findAll() {
        log.debug("Fetching all users, count={}", registry.count());
        return registry.findAll();
    }

    public boolean exists(String email) {
        return registry.exists(email);
    }
}