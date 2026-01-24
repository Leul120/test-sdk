package com.example.sdkdemo.controller;

import com.example.sdkdemo.dto.ApiResponse;
import com.example.sdkdemo.dto.CreateUserRequest;
import com.example.sdkdemo.dto.UpdateUserRequest;
import com.example.sdkdemo.model.User;
import com.example.sdkdemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for user management operations.
 * AI-Synapse monitoring is automatic via the filter - no annotations needed!
 */
@RestController
@RequestMapping("/api")
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        Optional<User> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get(), "User retrieved successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: " + id));
        }
    }

    /**
     * Get user by email
     */
    @GetMapping("/users/by-email")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@RequestParam String email) {
        log.info("Fetching user by email: {}", email);
        Optional<User> user = userService.getUserByEmail(email);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get(), "User retrieved successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with email: " + email));
        }
    }

    /**
     * Create a new user
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .build();
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }

    /**
     * Update an existing user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        
        User userDetails = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .active(request.getActive())
                .build();
        
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    /**
     * Activate a user
     */
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Long id) {
        log.info("Activating user with id: {}", id);
        User user = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User activated successfully"));
    }

    /**
     * Deactivate a user
     */
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<User>> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user with id: {}", id);
        User user = userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User deactivated successfully"));
    }

    /**
     * Get users by role
     */
    @GetMapping("/users/by-role")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@RequestParam String role) {
        log.info("Fetching users by role: {}", role);
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    /**
     * Get user statistics
     */
    @GetMapping("/users/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        log.info("Fetching user statistics");
        long totalUsers = userService.getUserCount();
        List<User> adminUsers = userService.getUsersByRole("ADMIN");
        List<User> regularUsers = userService.getUsersByRole("USER");
        
        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "adminCount", adminUsers.size(),
                "userCount", regularUsers.size(),
                "activeUsers", regularUsers.stream().mapToLong(u -> u.getActive() ? 1 : 0).sum() + 
                                   adminUsers.stream().mapToLong(u -> u.getActive() ? 1 : 0).sum()
        );
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }

    /**
     * Perform complex user operation (for testing error scenarios)
     */
    @PostMapping("/users/{id}/operations")
    public ResponseEntity<ApiResponse<User>> performOperation(
            @PathVariable Long id, 
            @RequestParam String operation) {
        log.info("Performing operation '{}' on user: {}", operation, id);
        
        try {
            User user = userService.performComplexUserOperation(id, operation);
            return ResponseEntity.ok(ApiResponse.success(user, "Operation completed successfully"));
        } catch (Exception e) {
            log.error("Operation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Operation failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "UP", "service", "User Management API"), 
                "Service is healthy"
        ));
    }

    /**
     * Endpoint to trigger errors for testing
     */
    @GetMapping("/users/error")
    public ResponseEntity<ApiResponse<String>> triggerError() {
        log.info("Triggering a deliberate error for verification");
        throw new RuntimeException("Deliberate error for AI-Synapse verification");
    }

    /**
     * Endpoint to test validation errors
     */
    @PostMapping("/users/invalid")
    public ResponseEntity<ApiResponse<User>> createInvalidUser(@RequestBody CreateUserRequest request) {
        log.info("Attempting to create invalid user: {}", request.getEmail());
        
        // This will trigger validation errors
        User user = User.builder()
                .name("") // Invalid: empty name
                .email("invalid-email") // Invalid: bad email format
                .role("INVALID_ROLE") // Invalid: not USER or ADMIN
                .build();
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }
}
