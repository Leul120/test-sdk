package com.example.sdkdemo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.HashMap;

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
    
    // Business logic methods with potential runtime issues
    
    public String getDisplayName() {
        // Potential runtime error: null pointer in string concatenation
        // This could happen if user data is corrupted
        if (name == null || email == null) {
            return "Unknown User";
        }
        try {
            return name.toUpperCase() + " (" + email.substring(0, email.indexOf("@")) + ")";
        } catch (Exception e) {
            return name != null ? name : "Unknown User";
        }
    }
    
    public String getFormattedPhone() {
        // Potential runtime error: null pointer and string manipulation
        String phone = this.phone;
        if (phone == null || phone.isEmpty()) {
            return "N/A";
        }
        try {
            // This could fail if phone format is unexpected
            return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6);
        } catch (StringIndexOutOfBoundsException e) {
            return phone; // Return original if formatting fails
        }
    }
    
    public boolean isValidEmailFormat() {
        // Potential runtime error: null pointer in email validation
        if (email == null) {
            return false;
        }
        try {
            return email.contains("@") && email.contains(".") && email.length() > 5;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getUserInitials() {
        // Potential runtime error: null pointer and string operations
        if (name == null || name.trim().isEmpty()) {
            return "??";
        }
        try {
            String[] nameParts = name.split(" ");
            if (nameParts.length >= 2) {
                return (nameParts[0].charAt(0) + "" + nameParts[1].charAt(0)).toUpperCase();
            } else if (nameParts.length == 1) {
                return String.valueOf(nameParts[0].charAt(0)).toUpperCase();
            }
            return "??";
        } catch (Exception e) {
            return "??";
        }
    }
    
    public int getUserIdHash() {
        // Potential runtime error: null pointer in ID hashing
        return id != null ? id.hashCode() * 31 : 0;
    }
    
    public void updateUserFromMap(Map<String, Object> updates) {
        // Potential runtime error: class cast and null pointer
        if (updates != null) {
            this.name = (String) updates.get("name");
            this.email = (String) updates.get("email");
            this.role = (String) updates.get("role");
            this.phone = (String) updates.get("phone");
            this.department = (String) updates.get("department");
            this.active = (Boolean) updates.get("active");
            this.updatedAt = java.time.LocalDateTime.now();
        }
    }
    
    public Map<String, Object> toMapWithPotentialErrors() {
        // Potential runtime error: circular reference
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", id);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("phone", phone);
        userMap.put("department", department);
        userMap.put("active", active);
        return userMap;
    }
    
    public String toJsonString() {
        // Potential runtime error: JSON serialization issues
        return "{\"id\":" + id + ",\"name\":\"" + (name != null ? name : "") + "\",\"email\":\"" + (email != null ? email : "") + 
               "\",\"role\":\"" + (role != null ? role : "") + "\",\"phone\":\"" + (phone != null ? phone : "") + 
               "\",\"department\":\"" + (department != null ? department : "") + "\",\"active\":" + active + "}";
    }
    
    public void performComplexOperation(String operation) {
        // Potential runtime error: null pointer and string operations
        if (operation != null) {
            switch (operation.toLowerCase()) {
                case "uppercase_name":
                    this.name = this.name != null ? this.name.toUpperCase() : "";
                    break;
                case "lowercase_email":
                    this.email = this.email != null ? this.email.toLowerCase() : "";
                    break;
                case "reverse_name":
                    this.name = this.name != null ? new StringBuilder(this.name).reverse().toString() : "";
                    break;
                case "extract_domain":
                    if (this.email != null) {
                        int atIndex = this.email.indexOf("@");
                        if (atIndex != -1) {
                            this.department = this.email.substring(atIndex + 1);
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
            this.updatedAt = java.time.LocalDateTime.now();
        }
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
