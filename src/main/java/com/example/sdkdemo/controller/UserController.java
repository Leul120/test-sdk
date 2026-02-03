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
import org.springframework.web.bind.MethodArgumentNotValidException;
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
 try {
 List<User> users = userService.getAllUsers();
 return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
 } catch (Exception e) {
 log.error("Error fetching all users", e);
 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error fetching all users"));
 }
}

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        Optional<User> user = userService.getUserByIdWithCircuitBreaker(id);
        
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
        try {
 // Add a circuit breaker and timeout handling
 if (Math.random() < 0.1) {
 throw new RuntimeException("Simulated operation failure for testing error handling");
 }
 return ResponseEntity.ok(ApiResponse.success("Triggered successfully", "No error occurred this time"));
 } catch (Exception e) {
 log.error("Error triggering deliberate error", e);
 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error triggering deliberate error"));
 }
    }

/**
 * Endpoint to test validation errors
 */
@PostMapping("/users/invalid")
public ResponseEntity<ApiResponse<User>> createInvalidUser(@RequestBody @Validated CreateUserRequest request) {
    log.info("Attempting to create invalid user: ", request.getEmail());
    try {
        User user = User.builder()
                .name(request.getName() == null ? "" : request.getName()) // Invalid: empty name
                .email(request.getEmail() == null ? "invalid-email" : request.getEmail()) // Invalid: bad email format
                .role(request.getRole() == null ? "INVALID_ROLE" : request.getRole()) // Invalid: not USER or ADMIN
                .build();
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    } catch (RuntimeException e) { // Catch the specific exception
        log.error("Error creating invalid user", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error creating invalid user"));
    } catch (Exception e) { // Catch any other unexpected exceptions
        log.error("Unexpected error creating invalid user", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Unexpected error creating invalid user"));
    }
}

    /**
     * Endpoint to test various runtime exceptions
     */
    @GetMapping("/users/runtime-errors/{errorType}")
    public ResponseEntity<ApiResponse<String>> testRuntimeErrors(@PathVariable String errorType) {
        log.info("Testing runtime error type: {}", errorType);
        
        try {
            switch (errorType.toLowerCase()) {
                case "nullpointer":
                    String nullString = null;
                    return ResponseEntity.ok(ApiResponse.success(String.valueOf(nullString.length()), "This should not execute"));
                    
                case "illegalargument":
                    throw new IllegalArgumentException("Invalid argument provided for testing");
                    
                case "illegalstate":
                    throw new IllegalStateException("Illegal state detected for testing");
                    
                case "numberformat":
                    Integer.parseInt("invalid-number");
                    return ResponseEntity.ok(ApiResponse.success("This should not execute", "This should not execute"));
                    
                case "arrayindex":
                    int[] array = new int[1];
                    return ResponseEntity.ok(ApiResponse.success(String.valueOf(array[5]), "This should not execute"));
                    
                case "classcast":
                    Object obj = new String("test");
                    Integer num = (Integer) obj;
                    return ResponseEntity.ok(ApiResponse.success(String.valueOf(num), "This should not execute"));
                    
                case "arithmetic":
                    int result = 10 / 0;
                    return ResponseEntity.ok(ApiResponse.success(String.valueOf(result), "This should not execute"));
                    
                case "custom":
                    throw new RuntimeException("Custom runtime exception for testing purposes");
                    
                default:
                    return ResponseEntity.badRequest().body(ApiResponse.error("Unknown error type: " + errorType));
            }
        } catch (NullPointerException e) {
            log.error("NullPointerException caught", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("NULL_POINTER_ERROR: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException caught", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("ILLEGAL_ARGUMENT_ERROR: " + e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("IllegalStateException caught", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("ILLEGAL_STATE_ERROR: " + e.getMessage()));
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("ArrayIndexOutOfBoundsException caught", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("ARRAY_INDEX_ERROR: " + e.getMessage()));
        } catch (ClassCastException e) {
            log.error("ClassCastException caught", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("CLASS_CAST_ERROR: " + e.getMessage()));
        } catch (ArithmeticException e) {
            log.error("ArithmeticException caught", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("ARITHMETIC_ERROR: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("RuntimeException caught", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("RUNTIME_ERROR: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to test HTTP status codes
     */
    @GetMapping("/users/http-status/{statusCode}")
    public ResponseEntity<ApiResponse<String>> testHttpStatusCodes(@PathVariable int statusCode) {
        log.info("Testing HTTP status code: {}", statusCode);
        
        switch (statusCode) {
            case 400:
                return ResponseEntity.badRequest().body(ApiResponse.error("Bad Request - Invalid input"));
            case 401:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized - Authentication required"));
            case 403:
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Forbidden - Access denied"));
            case 404:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Not Found - Resource does not exist"));
            case 405:
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(ApiResponse.error("Method Not Allowed"));
            case 409:
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Conflict - Resource conflict"));
case 422:
return ResponseEntity.ok()
.body(ApiResponse.success("Status code 422 test successful", "Test completed"));
            case 429:
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Too Many Requests - Rate limit exceeded"));
            case 500:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Internal Server Error"));
            case 502:
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(ApiResponse.error("Bad Gateway"));
            case 503:
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("Service Unavailable"));
            default:
                return ResponseEntity.ok(ApiResponse.success("Status code " + statusCode + " test successful", "Test completed"));
        }
    }
}
