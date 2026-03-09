package com.collabhub.service;

import com.collabhub.domain.User;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(String name, String email, String role, String rawPassword) {
        LOG.debug("Creating user email={} role={}", email, role);
        if (userRepository.existsByEmail(email)) {
            LOG.warn("Duplicate user attempt: email={}", email);
            throw new DuplicateResourceException("User", email);
        }
        User user = new User(name, email, role, rawPassword);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        LOG.info("User created: name='{}' email={} role={}", name, email, role);
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable("users") // cache the full user list
    public User getByEmail(String email) {
        LOG.debug("Fetching user email={}", email);
        return userRepository.findByEmail(email).orElseThrow(() -> {
            LOG.warn("User not found: email={}", email);
            return new ResourceNotFoundException("User", email);
        });
    }

    @Transactional(readOnly = true)
    public User getById(java.util.UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        LOG.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void setPassword(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
