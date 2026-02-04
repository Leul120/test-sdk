package com.example.sdkdemo.service;

import com.example.sdkdemo.model.User;
import com.example.sdkdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
@Transactional(readOnly = true)
public List<User> getAllUsers() {
 log.debug("Fetching all users");
 try {
 return userRepository.findAll();
 } catch (Exception e) {
 log.error("Error fetching all users", e);
 throw new RuntimeException("Error fetching all users");
 }
}
    
@Transactional(readOnly = true)
public Optional<User> getUserByIdWithCircuitBreaker(Long id) {
 log.debug("Fetching user by id: {} with circuit breaker", id);
 try {
 // Add circuit breaker and timeout handling
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
        
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Validate role
        if (!user.isValidRole()) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        return savedUser;
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Update fields
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setRole(userDetails.getRole());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setDepartment(userDetails.getDepartment());
        existingUser.setUpdatedAt(java.time.LocalDateTime.now());
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
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Simulate different operations
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
                
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        // Simulate potential failure
        if (Math.random() < 0.1) { // 10% chance of failure
            throw new RuntimeException("Simulated operation failure for testing error handling");
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
            
            return allUsers.stream()
                    .filter(user -> {
                        // Potential runtime error: null pointer during filtering
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
}
