package com.example.sdkdemo.exception;

public class InsufficientInventoryException extends BusinessException {
    public InsufficientInventoryException(String sku, int requested, int available) {
        super("INSUFFICIENT_INVENTORY",
                String.format("SKU %s requested %d units, only %d available", sku, requested, available));
    }
}
