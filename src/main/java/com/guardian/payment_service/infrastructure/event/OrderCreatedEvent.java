package com.guardian.payment_service.infrastructure.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class OrderCreatedEvent {
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private Instant createdAt;
}