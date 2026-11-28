package com.example.sdkdemo.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private String actorEmail;
}
