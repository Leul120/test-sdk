package com.example.sdkdemo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    private String sku;
    private Integer quantity;
}
