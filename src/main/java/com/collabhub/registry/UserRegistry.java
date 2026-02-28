package com.collabhub.registry;

import com.collabhub.domain.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRegistry {

    // Email → User. Email is unique per user, perfect map key
    private final Map<String, User> store = new HashMap<>();

    public User save(User user) {
        store.put(user.getEmail(), user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        // Optional — a container that may or may not hold a value
        // Forces the caller to handle the "not found" case — no surprise nulls
        return Optional.ofNullable(store.get(email));
    }

    public Collection<User> findAll() {
        return store.values();
    }

    public boolean exists(String email) {
        return store.containsKey(email);
    }

    public int count() {
        return store.size();
    }
}