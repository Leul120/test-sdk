package com.example.sdkdemo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSummaryDto {
    private String invoiceNumber;
    private String orderNumber;
    private String departmentName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate dueDate;
}
