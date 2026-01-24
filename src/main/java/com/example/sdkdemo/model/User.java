package com.example.sdkdemo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Role is required")
    @Column(nullable = false)
    private String role; // USER, ADMIN
    
    private String phone;
    private String department;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    // Audit fields
    private String createdBy;
    private String updatedBy;
    
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;
    
    private java.time.LocalDateTime updatedAt;
    
    // Validation method
    public boolean isValidRole() {
        return "USER".equals(role) || "ADMIN".equals(role);
    }
    
    // Business logic
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    public void deactivate() {
        this.active = false;
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
