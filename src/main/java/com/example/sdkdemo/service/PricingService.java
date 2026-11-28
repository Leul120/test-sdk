package com.example.sdkdemo.service;

import com.example.sdkdemo.model.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal applyDiscounts(BigDecimal subtotal, BigDecimal discountPercent, String promoCode) {
        BigDecimal effectiveDiscount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        if ("ENTERPRISE20".equals(promoCode.trim().toUpperCase())) {
            effectiveDiscount = effectiveDiscount.max(new BigDecimal("20"));
        }
        BigDecimal multiplier = BigDecimal.ONE.subtract(
                effectiveDiscount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
