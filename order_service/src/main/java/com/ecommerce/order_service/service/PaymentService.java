package com.ecommerce.order_service.service;


import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private static final double FAILURE_RATE = 0.15;

    public boolean processPayment(java.math.BigDecimal amount) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ThreadLocalRandom.current().nextDouble() > FAILURE_RATE;
    }
}