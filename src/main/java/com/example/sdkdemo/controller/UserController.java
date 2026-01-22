package com.example.sdkdemo.controller;

import com.aisynapse.sdk.annotation.SynapseLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

/**
 * A NORMAL REST controller for a typical application.
 * The @SynapseLog annotation is the ONLY thing needed for AI-Synapse monitoring.
 * No explicit SDK calls are made - everything is automatic.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    // In-memory data store (simulates a database)
    private final List<Map<String, Object>> users = new ArrayList<>(List.of(
            Map.of("id", 1, "name", "Alice Johnson", "email", "alice@example.com"),
            Map.of("id", 2, "name", "Bob Smith", "email", "bob@example.com"),
            Map.of("id", 3, "name", "Charlie Brown", "email", "charlie@example.com")
    ));

    /**
     * Get all users
     */
    @GetMapping("/users")
    @SynapseLog(source = "demo-app", endpoint = "/api/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    @SynapseLog(source = "demo-app", endpoint = "/api/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        return users.stream()
                .filter(u -> (int) u.get("id") == id)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new user
     */
    @PostMapping("/users")
    @SynapseLog(source = "demo-app", endpoint = "/api/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        int newId = users.size() + 1;
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("id", newId);
        newUser.put("name", userData.getOrDefault("name", "New User"));
        newUser.put("email", userData.getOrDefault("email", "new@example.com"));
        users.add(newUser);
        return ResponseEntity.status(201).body(newUser);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/users/error")
    @SynapseLog(endpoint = "/api/users/error")
    public String triggerError() {
        log.info("Triggering a deliberate error for verification");
        throw new RuntimeException("Deliberate error for AI-Synapse verification");
    }
}
