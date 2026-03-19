package com.example.sdkdemo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    @NotBlank(message = "Search query is required")
    @Size(max = 500, message = "Search query must be less than 500 characters")
    private String query;
}
