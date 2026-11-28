package com.example.sdkdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final List<String> dispatchedMessages = Collections.synchronizedList(new ArrayList<>());

    public void notifyOrderConfirmed(String recipientEmail, String orderNumber, String totalFormatted) {
        String msg = String.format("TO:%s | Order %s confirmed (%s)", recipientEmail, orderNumber, totalFormatted);
        log.info("[NOTIFICATION] {}", msg);
        dispatchedMessages.add(msg);
    }

    public void notifyPaymentSettled(String recipientEmail, String invoiceNumber, String transactionRef) {
        String msg = String.format("TO:%s | Invoice %s settled (ref=%s)", recipientEmail, invoiceNumber, transactionRef);
        log.info("[NOTIFICATION] {}", msg);
        dispatchedMessages.add(msg);
    }

    public List<String> getRecentNotifications() {
        return new ArrayList<>(dispatchedMessages);
    }
}
