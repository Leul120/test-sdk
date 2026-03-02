package com.example.sdkdemo.config;

import com.example.sdkdemo.model.User;
import com.example.sdkdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing application data...");
        
        try {
            if (userRepository.count() == 0) {
                log.info("No users found, creating initial demo data");
                
                // Create basic users with realistic data
                userRepository.save(User.builder()
                        .name("Alice Johnson")
                        .email("alice.johnson@company.com")
                        .role("USER")
                        .phone("+1-555-0123")
                        .department("Engineering")
                        .createdBy("system")
                        .updatedBy("system")
                        .build());
                
                userRepository.save(User.builder()
                        .name("Bob Smith")
                        .email("bob.smith@company.com")
                        .role("USER")
                        .phone("+1-555-0124")
                        .department("Marketing")
                        .createdBy("system")
                        .updatedBy("system")
                        .build());
                
                userRepository.save(User.builder()
                        .name("Charlie Brown")
                        .email("charlie.brown@company.com")
                        .role("ADMIN")
                        .phone("+1-555-0125")
                        .department("IT")
                        .createdBy("system")
                        .updatedBy("system")
                        .build());
                
                // Create a user with potentially problematic data for testing
                userRepository.save(User.builder()
                        .name("Test User") // Normal name
                        .email("test.user@company.com")
                        .role("USER")
                        .phone(null) // Null phone - could cause issues in formatting
                        .department("Testing")
                        .createdBy("system")
                        .updatedBy("system")
                        .build());
                
                log.info("Demo data initialized successfully");
            } else {
                log.info("Users already exist, skipping initialization");
                
                // Verify data integrity - this could uncover runtime issues
                try {
                    List<User> existingUsers = userRepository.findAll();
                    log.info("Found {} existing users", existingUsers.size());
                    
                    // Check for data issues that could cause runtime errors
                    for (User user : existingUsers) {
                        if (user.getEmail() != null && user.getEmail().contains("@")) {
                            // This could fail if email is malformed
                            String domain = user.getEmail().substring(user.getEmail().indexOf("@") + 1);
                            log.trace("User {} has domain: {}", user.getId(), domain);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error verifying existing data integrity", e);
                    // Don't fail startup - just log the issue
                }
            }
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            // Allow application to start even if data initialization fails
        }
    }
}
