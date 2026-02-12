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
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
        try {
            User user = User.builder()
                    .name(request.getName() == null ? "" : request.getName())
                    .email(request.getEmail() == null ? "invalid-email" : request.getEmail())
                    .role(request.getRole() == null ? "INVALID_ROLE" : request.getRole())
                    .build();
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdUser, "User created successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating invalid user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Unexpected error creating invalid user"));
        }
    }

    /**
     * Search users with complex filters (may cause runtime errors)
     */
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Searching users with filters - name: {}, email: {}, role: {}, department: {}, active: {}, page: {}, size: {}", 
                name, email, role, department, active, page, size);
        
        try {
            // Potential runtime error: division by zero if size is 0
            if (size <= 0) {
                throw new IllegalArgumentException("Page size must be a positive integer.");
            }
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be a non-negative integer.");
            }
            
            // Potential runtime error: null pointer if filters are not handled properly
            List<User> users = userService.searchUsers(name, email, role, department, active);
            
            // Potential runtime error: array index out of bounds if page is invalid
            int startIndex = page * size;
            if (startIndex >= users.size()) {
                return ResponseEntity.ok(ApiResponse.success(List.of(), "No users found for the given page"));
            }
            
            int endIndex = Math.min(startIndex + size, users.size());
            List<User> paginatedUsers = users.subList(startIndex, endIndex);
            
            return ResponseEntity.ok(ApiResponse.success(paginatedUsers, 
                    String.format("Found %d users (page %d, showing %d results)", users.size(), page, paginatedUsers.size())));
                
        } catch (IllegalArgumentException e) {
            log.error("Invalid search parameters", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid search parameters: " + e.getMessage()));
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds during pagination", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid page number: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error during user search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error during user search: " + e.getMessage()));
        }
    }

    /**
     * Bulk operations on users (may cause runtime errors)
     */
    @PostMapping("/users/bulk")
    public ResponseEntity<ApiResponse<List<User>>> bulkCreateUsers(@RequestBody List<@Valid CreateUserRequest> requests) {
        log.info("Bulk creating {} users", requests.size());
        
        try {
            // Potential runtime error: null pointer if requests list is null
            if (requests == null) {
                throw new IllegalArgumentException("User requests list cannot be null");
            }
            
            // Potential runtime error: out of memory if list is too large
            if (requests.size() > 1000) {
                throw new IllegalArgumentException("Cannot create more than 1000 users in a single request");
            }
            
            List<User> createdUsers = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (CreateUserRequest request : requests) {
                try {
                    User user = User.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .role(request.getRole())
                            .phone(request.getPhone())
                            .department(request.getDepartment())
                            .build();
                    
                    User createdUser = userService.createUser(user);
                    createdUsers.add(createdUser);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to create user: {}", request.getEmail(), e);
                    failureCount++;
                }
            }
            
            String message = String.format("Bulk operation completed. Success: %d, Failures: %d", successCount, failureCount);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdUsers, message));
                    
        } catch (IllegalArgumentException e) {
            log.error("Invalid bulk operation parameters", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid bulk operation: " + e.getMessage()));
        } catch (OutOfMemoryError e) {
            log.error("Out of memory during bulk operation", e);
            return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE)
                    .body(ApiResponse.error("Insufficient memory for bulk operation"));
        } catch (Exception e) {
            log.error("Error during bulk user creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error during bulk user creation: " + e.getMessage()));
        }
    }

    /**
     * Export users to different formats (may cause runtime errors)
     */
    @GetMapping("/users/export")
    public ResponseEntity<ApiResponse<String>> exportUsers(@RequestParam String format) {
        log.info("Exporting users in format: {}", format);
        
        try {
            List<User> users = userService.getAllUsers();
            
            switch (format.toLowerCase()) {
                case "json":
                    try {
                        String jsonResult = convertUsersToJson(users);
                        return ResponseEntity.ok(ApiResponse.success(jsonResult, "Users exported as JSON"));
                    } catch (Exception e) {
                        log.error("JSON serialization error during export", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("JSON serialization error during export: " + e.getMessage()));
                    }
                case "csv":
                    try {
                        String csvResult = convertUsersToCsv(users);
                        return ResponseEntity.ok(ApiResponse.success(csvResult, "Users exported as CSV"));
                    } catch (Exception e) {
                        log.error("CSV formatting error during export", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("CSV formatting error during export: " + e.getMessage()));
                    }
                case "xml":
                    try {
                        String xmlResult = convertUsersToXml(users);
                        return ResponseEntity.ok(ApiResponse.success(xmlResult, "Users exported as XML"));
                    } catch (Exception e) {
                        log.error("XML conversion error during export", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("XML conversion error during export: " + e.getMessage()));
                    }
                default:
                    throw new IllegalArgumentException("Unsupported export format: " + format);
            }
        } catch (ClassCastException e) {
            log.error("Type conversion error during export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Type conversion error during export: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid export format", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid export format: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error during user export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error during user export: " + e.getMessage()));
        }
    }

    /**
     * Complex user analytics (may cause runtime errors)
     */
    @GetMapping("/users/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Generating user analytics from {} to {}", startDate, endDate);
        
        try {
            // Potential runtime error: date parsing error
            if (startDate != null && endDate != null) {
                // Simulate date parsing that could fail
                if (startDate.equals("invalid") || endDate.equals("invalid")) {
                    throw new IllegalArgumentException("Invalid date format");
                }
            }
            
            List<User> allUsers = userService.getAllUsers();
            
            // Potential runtime error: division by zero
            double totalUsers = allUsers.size();
            if (totalUsers == 0) {
                throw new IllegalStateException("No users found for analytics");
            }
            
            // Potential runtime error: null pointer in stream operations
            Map<String, Long> roleDistribution = allUsers.stream()
                    .filter(user -> user.getRole() != null)
                    .collect(Collectors.groupingBy(
                            User::getRole, 
                            Collectors.counting()
                    ));
            
            Map<String, Long> departmentDistribution = allUsers.stream()
                    .filter(user -> user.getDepartment() != null)
                    .collect(Collectors.groupingBy(
                            User::getDepartment, 
                            Collectors.counting()
                    ));
            
            // Potential runtime error: arithmetic exception
            long activeUsers = allUsers.stream()
                    .filter(user -> user.getActive() != null && user.getActive())
                    .count();
            
            double activePercentage = (activeUsers / totalUsers) * 100;
            
            Map<String, Object> analytics = Map.of(
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "activePercentage", activePercentage,
                    "roleDistribution", roleDistribution,
                    "departmentDistribution", departmentDistribution,
                    "generatedAt", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics generated successfully"));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid date parameters for analytics", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid date parameters: " + e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("No data available for analytics", e);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.error("No data available for analytics: " + e.getMessage()));
        } catch (ArithmeticException e) {
            log.error("Arithmetic error during analytics calculation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Calculation error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating analytics: " + e.getMessage()));
        }
    }

    // Helper methods for export functionality (may cause runtime errors)
    private String convertUsersToJson(List<User> users) {
        try {
            // Potential runtime error: JSON serialization failure
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                json.append("{");
                json.append("\"id\":").append(user.getId()).append(",");
                json.append("\"name\":\"").append(user.getName() != null ? user.getName() : "").append("\",");
                json.append("\"email\":\"").append(user.getEmail() != null ? user.getEmail() : "").append("\",");
                json.append("\"role\":\"").append(user.getRole() != null ? user.getRole() : "").append("\"");
                json.append("}");
                if (i < users.size() - 1) json.append(",");
            }
            json.append("]");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    private String convertUsersToCsv(List<User> users) {
        try {
            // Potential runtime error: CSV formatting failure
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Name,Email,Role,Department,Active\n");
            for (User user : users) {
                csv.append(user.getId()).append(",");
                csv.append(user.getName() != null ? user.getName() : "").append(",");
                csv.append(user.getEmail() != null ? user.getEmail() : "").append(",");
                csv.append(user.getRole() != null ? user.getRole() : "").append(",");
                csv.append(user.getDepartment() != null ? user.getDepartment() : "").append(",");
                csv.append(user.getActive() != null ? user.getActive() : false).append("\n");
            }
            return csv.toString();
        } catch (Exception e) {
            throw new RuntimeException("CSV formatting failed", e);
        }
    }

    private String convertUsersToXml(List<User> users) {
        try {
            // Potential runtime error: XML generation failure
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<users>\n");
            for (User user : users) {
                xml.append("  <user>\n");
                xml.append("    <id>").append(user.getId()).append("</id>\n");
                xml.append("    <name>").append(user.getName() != null ? user.getName() : "").append("</name>\n");
                xml.append("    <email>").append(user.getEmail() != null ? user.getEmail() : "").append("</email>\n");
                xml.append("    <role>").append(user.getRole() != null ? user.getRole() : "").append("</role>\n");
                xml.append("    <department>").append(user.getDepartment() != null ? user.getDepartment() : "").append("</department>\n");
                xml.append("    <active>").append(user.getActive() != null ? user.getActive() : false).append("</active>\n");
                xml.append("  </user>\n");
            }
            xml.append("</users>");
            return xml.toString();
        } catch (Exception e) {
            throw new RuntimeException("XML generation failed", e);
        }
    }
}
