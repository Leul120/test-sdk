package com.example.sdkdemo.config;

import com.example.sdkdemo.model.User;
import com.example.sdkdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Initializing demo data...");
            
            userRepository.save(User.builder()
                    .name("Alice Johnson")
                    .email("alice@example.com")
                    .role("USER")
                    .phone("+1234567890")
                    .department("Engineering")
                    .createdBy("system")
                    .updatedBy("system")
                    .build());
            
            userRepository.save(User.builder()
                    .name("Bob Smith")
                    .email("bob.smith@company.com")
                    .role("USER")
                    .phone("+0987654321")
                    .department("Marketing")
                    .createdBy("system")
                    .updatedBy("system")
                    .build());
            
            userRepository.save(User.builder()
                    .name("Charlie Brown")
                    .email("charlie.brown@company.com")
                    .role("ADMIN")
                    .phone("+1122334455")
                    .department("IT")
                    .createdBy("system")
                    .updatedBy("system")
                    .build());
            
            log.info("Demo data initialized successfully");
        }
    }
}
