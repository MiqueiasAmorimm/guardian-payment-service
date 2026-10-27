package com.guardian.payment_service.infrastructure.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PaymentApprovedEvent {
    private UUID orderId;
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;

    public PaymentApprovedEvent(UUID orderId, UUID paymentId, BigDecimal amount, String currency) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
    }
}