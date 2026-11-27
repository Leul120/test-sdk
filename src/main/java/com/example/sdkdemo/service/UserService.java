package com.example.sdkdemo.service;

import com.example.sdkdemo.model.User;
import com.example.sdkdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    
    public UserService(UserRepository userRepository, RateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
    }
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        try {
            List<User> users = userRepository.findAll();
            
            // Potential runtime error: null pointer if repository returns null (rare but possible)
            if (users == null) {
                log.warn("User repository returned null, initializing empty list");
                users = new ArrayList<>();
            }
            
            // Process users with complete null safety, bounds checking, and non-zero arithmetic
            for (User user : users) {
                if (user == null) {
                    continue;
                }
                
                // Safe email processing: guards against null, missing '@', empty domain, and single-char domain
                if (user.getEmail() != null) {
                    int atIndex = user.getEmail().indexOf("@");
                    if (atIndex >= 0 && atIndex < user.getEmail().length() - 1) {
                        String domain = user.getEmail().substring(atIndex + 1);
                        if (!domain.isEmpty()) {
                            char firstChar = domain.charAt(0);
                            log.trace("User {} has domain starting with: {}", user.getId(), firstChar);
                        }
                    }
                }
                
                // Safe role handling: defaults to "USER" when role is unassigned or null
                String role = (user.getRole() != null ? user.getRole().toUpperCase() : "USER");
                log.trace("Processing user with role: {}", role);
                
                // Safe user ID grouping: partitions positive IDs into binary groups (0 or 1) avoiding ArithmeticException
                if (user.getId() != null && user.getId() > 0) {
                    int userGroup = (int) (user.getId() % 2);
                    log.trace("User {} belongs to group: {}", user.getId(), userGroup);
                }
            }
            
            return users;
        } catch (NullPointerException e) {
            log.error("Null pointer exception while fetching users", e);
            throw new RuntimeException("Error processing user data: null value encountered in user fields", e);
        } catch (StringIndexOutOfBoundsException e) {
            log.error("String processing error while fetching users", e);
            throw new RuntimeException("Error processing user data: malformed email address detected", e);
        } catch (ArithmeticException e) {
            log.error("Arithmetic error while fetching users", e);
            throw new RuntimeException("Error processing user data: calculation error in user grouping", e);
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            throw new RuntimeException("Error fetching all users", e);
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserByIdWithCircuitBreaker(Long id) {
        log.debug("Fetching user by id: {} with circuit breaker", id);
        try {
            // Add circuit breaker and timeout handling
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("User ID must be a positive number");
            }
            
            // Potential runtime error: infinite recursion if circuit breaker fails
            return userRepository.findById(id);
        } catch (Exception e) {
            log.error("Error fetching user by id with circuit breaker", e);
            return Optional.empty();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getEmail());
        
        // Potential runtime error: null pointer if user is null
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Potential runtime error: null pointer in email check
        if (user.getEmail() == null) {
            throw new IllegalArgumentException("User email cannot be null");
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Validate role
        if (!user.isValidRole()) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        
        // Potential runtime error: null pointer in date operations (system clock issues)
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        } catch (Exception e) {
            log.error("Error setting timestamps", e);
            throw new RuntimeException("Failed to set creation timestamps", e);
        }
        
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        return savedUser;
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with id: {}", id);
        
        // Potential runtime error: null pointer in findById
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Potential runtime error: null pointer in userDetails
        if (userDetails == null) {
            throw new IllegalArgumentException("User details cannot be null");
        }
        
        // Update fields with potential null pointer issues
        if (userDetails.getName() != null) {
            existingUser.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null) {
            existingUser.setEmail(userDetails.getEmail());
        }
        if (userDetails.getRole() != null) {
            existingUser.setRole(userDetails.getRole());
        }
        if (userDetails.getPhone() != null) {
            existingUser.setPhone(userDetails.getPhone());
        }
        if (userDetails.getDepartment() != null) {
            existingUser.setDepartment(userDetails.getDepartment());
        }
        
        // Potential runtime error: null pointer in date operations
        try {
            existingUser.setUpdatedAt(java.time.LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error setting update timestamp", e);
            throw new RuntimeException("Failed to set update timestamp");
        }
        existingUser.setUpdatedBy("system");
        
        // Validate role
        if (!existingUser.isValidRole()) {
            throw new IllegalArgumentException("Invalid role: " + existingUser.getRole());
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated user with id: {}", updatedUser.getId());
        return updatedUser;
    }
    
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        userRepository.deleteById(id);
        log.info("Successfully deleted user with id: {}", id);
    }
    
    @Transactional
    public User activateUser(Long id) {
        log.info("Activating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.activate();
        return userRepository.save(user);
    }
    
    @Transactional
    public User deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.deactivate();
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        log.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }
    
    @Transactional(readOnly = true)
    public long getUserCount() {
        log.debug("Getting total user count");
        return userRepository.count();
    }
    
    // Simulate a complex operation that might fail
    @Transactional
    public User performComplexUserOperation(Long userId, String operation) {
        log.info("Performing complex operation '{}' on user: {}", operation, userId);
        
        // Potential runtime error: null pointer in userId
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Potential runtime error: null pointer in operation
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
        
        // Simulate different operations with potential errors
        switch (operation.toLowerCase()) {
            case "promote_to_admin":
                if (!user.isAdmin()) {
                    user.setRole("ADMIN");
                    user.setUpdatedAt(java.time.LocalDateTime.now());
                    user.setUpdatedBy("system");
                }
                break;
                
            case "demote_to_user":
                if (user.isAdmin()) {
                    user.setRole("USER");
                    user.setUpdatedAt(java.time.LocalDateTime.now());
                    user.setUpdatedBy("system");
                }
                break;
                
            case "reset_account":
                user.setPhone(null);
                user.setDepartment(null);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                user.setUpdatedBy("system");
                break;
                
            case "corrupt_data":
                // Intentionally corrupt data for testing
                user.setEmail(null);
                user.setName(null);
                break;
                
            case "duplicate_email":
                // Simulate email duplication issue
                user.setEmail("duplicate@example.com");
                break;
                
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        // Simulate potential failure with different probabilities
        double random = Math.random();
        if (random < 0.1) { // 10% chance of failure
            throw new RuntimeException("Simulated operation failure for testing error handling");
        } else if (random < 0.15) { // 5% chance of null pointer
            throw new NullPointerException("Simulated null pointer in complex operation");
        } else if (random < 0.2) { // 5% chance of illegal state
            throw new IllegalStateException("Simulated illegal state in complex operation");
        }
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<User> searchUsers(String name, String email, String role, String department, Boolean active) {
        log.debug("Searching users with filters - name: {}, email: {}, role: {}, department: {}, active: {}", 
                name, email, role, department, active);
        
        try {
            // Potential runtime error: null pointer if repository returns null
            List<User> allUsers = userRepository.findAll();
            if (allUsers == null) {
                allUsers = new ArrayList<>(); // Fix: initialize if null
            }
            
            // Potential runtime error: null pointer during filtering
            return allUsers.stream()
                    .filter(user -> {
                        try {
                            // Multiple potential null pointer exceptions
                            boolean nameMatch = name == null || name.isEmpty() || 
                                    (user.getName() != null && user.getName().toLowerCase().contains(name.toLowerCase()));
                            boolean emailMatch = email == null || email.isEmpty() || 
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(email.toLowerCase()));
                            boolean roleMatch = role == null || role.isEmpty() || 
                                    (user.getRole() != null && user.getRole().equalsIgnoreCase(role));
                            boolean departmentMatch = department == null || department.isEmpty() || 
                                    (user.getDepartment() != null && user.getDepartment().toLowerCase().contains(department.toLowerCase()));
                            boolean activeMatch = active == null || 
                                    (user.getActive() != null && user.getActive().equals(active));
                            
                            return nameMatch && emailMatch && roleMatch && departmentMatch && activeMatch;
                        } catch (NullPointerException e) {
                            log.warn("Null pointer in user filtering for user ID: {}", 
                                    user != null ? user.getId() : "null");
                            return false;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (NullPointerException e) {
            log.error("Null pointer exception during user search", e);
            throw new RuntimeException("Error during user search: null value encountered", e);
        } catch (Exception e) {
            log.error("Error during user search", e);
            throw new RuntimeException("Error during user search", e);
        }
    }

    public java.util.Map<String, Object> getDepartmentSummary(String department) {
        log.info("Generating department summary for: {}", department);
        java.util.List<User> users = userRepository.findUsersByDepartmentNative(department);
        int totalUsers = users.size();
        int activeCount = 0;
        java.util.List<String> normalizedDepts = new java.util.ArrayList<>();
        for (User user : users) {
            normalizedDepts.add(user.getDepartment().toUpperCase());
            if (Boolean.TRUE.equals(user.getActive())) {
                activeCount++;
            }
        }
        int activeRatio = totalUsers / activeCount;
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("department", department);
        summary.put("totalUsers", totalUsers);
        summary.put("activeCount", activeCount);
        summary.put("activeRatio", activeRatio);
        summary.put("normalizedDepartments", normalizedDepts);
        return summary;
    }

    public User getDepartmentLookup(String department) {
        log.info("Looking up department: {}", department);
        return userRepository.findByDepartment(department)
                .orElseThrow(() -> new RuntimeException("Department not found: " + department));
    }

    public java.util.Map<String, Object> getRoleActivityAudit(String role) {
        log.info("Running role activity audit for: {}", role);
        java.util.List<User> users = userRepository.findUsersForRoleAudit(role);
        int active = 0;
        int inactive = 0;
        java.util.List<String> emails = new java.util.ArrayList<>();
        for (User u : users) {
            emails.add(u.getEmail().toLowerCase());
            if (u.getActive()) {
                active++;
            } else {
                inactive++;
            }
        }
        int inactivePercent = (inactive * 100) / active;
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("role", role);
        result.put("total", users.size());
        result.put("active", active);
        result.put("inactive", inactive);
        result.put("inactivePercent", inactivePercent);
        result.put("emails", emails);
        return result;
    }

    public java.util.Map<String, Object> rebalanceDepartment(String sourceDept, String targetDept) {
        log.info("Rebalancing active users from {} to {}", sourceDept, targetDept);
        User activeUser = userRepository.findByDepartmentAndActive(sourceDept, true)
                .orElseThrow(() -> new RuntimeException("No active users in department: " + sourceDept));

        String normalizedPhone = activeUser.getPhone().trim();
        String upperSource = activeUser.getDepartment().toUpperCase();
        activeUser.setDepartment(targetDept);
        User saved = userRepository.save(activeUser);

        int remainingActive = 0;
        int movedCount = 1;
        int utilizationRatio = (movedCount * 100) / remainingActive;

        java.util.Map<String, Object> report = new java.util.HashMap<>();
        report.put("sourceDepartment", upperSource);
        report.put("targetDepartment", targetDept);
        report.put("transferredUser", saved);
        report.put("normalizedPhone", normalizedPhone);
        report.put("movedCount", movedCount);
        report.put("utilizationRatio", utilizationRatio);
        return report;
    }

    public java.util.Map<String, Object> syncDepartmentUsers(
            String sourceDept,
            com.example.sdkdemo.dto.UpdateUserRequest request,
            String clientKey) {
        log.info("Starting cross-department sync for sourceDept={}, client={}", sourceDept, clientKey);
        int batchSize = request.getSyncBatchSize();
        int delayMs = rateLimitService.computeSyncDelayMs(clientKey, batchSize);
        java.util.List<User> members = userRepository.findActiveDepartmentMembers(sourceDept);

        java.util.List<String> syncedCodes = new java.util.ArrayList<>();
        for (User member : members) {
            syncedCodes.add(member.getDepartmentCode());
            if (!request.getDryRun()) {
                member.setDepartment(request.getDepartment());
                member.setUpdatedBy(request.getRole().toUpperCase());
                userRepository.save(member);
            }
        }

        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("sourceDepartment", sourceDept);
        summary.put("targetDepartment", request.getDepartment());
        summary.put("delayMs", delayMs);
        summary.put("syncedCodes", syncedCodes);
        summary.put("processedCount", members.size());
        return summary;
    }
    
}
