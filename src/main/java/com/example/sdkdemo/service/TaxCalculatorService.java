package com.example.sdkdemo.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class TaxCalculatorService {

    private static final Map<String, BigDecimal> REGION_TAX_RATES = Map.of(
            "US-EAST", new BigDecimal("0.0725"),
            "US-WEST", new BigDecimal("0.0875"),
            "EU-CENTRAL", new BigDecimal("0.1900"),
            "UK", new BigDecimal("0.2000")
    );

    public BigDecimal calculateTax(BigDecimal taxableAmount, String shippingRegion) {
        String normalizedRegion = shippingRegion.trim().toUpperCase();
        BigDecimal rate = REGION_TAX_RATES.getOrDefault(normalizedRegion, new BigDecimal("0.0500"));
        return taxableAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEffectiveTaxRatePercent(BigDecimal taxAmount, BigDecimal subtotal) {
        return taxAmount.multiply(BigDecimal.valueOf(100)).divide(subtotal);
    }
}
