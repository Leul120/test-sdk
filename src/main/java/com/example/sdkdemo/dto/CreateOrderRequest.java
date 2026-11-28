package com.example.sdkdemo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private Long userId;
    private Long departmentId;
    private String shippingRegion;
    private BigDecimal discountPercent;
    private String promoCode;
    private List<OrderItemRequest> items;
}
