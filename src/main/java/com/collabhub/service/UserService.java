package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true) // clear cache on write
    public User createUser(String name, String email, String role) {
        log.debug("Creating user email={} role={}", email, role);
        if (userRepository.existsByEmail(email)) {
            log.warn("Duplicate user attempt: email={}", email);
            throw new DuplicateResourceException("User", email);
        }
        User user = new User(name, email, role);
        User saved = userRepository.save(user);
        log.info("User created: name='{}' email={} role={}", name, email, role);
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable("users")  // cache the full user list
    public User getByEmail(String email) {
        log.debug("Fetching user email={}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: email={}", email);
                    return new ResourceNotFoundException("User", email);
                });
    }

    @Transactional(readOnly = true)
    public User getById(java.util.UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }
}