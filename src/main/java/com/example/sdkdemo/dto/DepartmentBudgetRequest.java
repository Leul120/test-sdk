package com.example.sdkdemo.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentBudgetRequest {
    private String departmentName;
    private BigDecimal additionalBudget;
    private String costCenterCode;
    private String requestedByEmail;
}
