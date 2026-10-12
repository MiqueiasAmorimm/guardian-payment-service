package com.guardian.payment_service.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedConsumer {

    @KafkaListener(topics = "order.created")
    public void consume(OrderCreatedEvent event) {
        log.info("Received order.created: orderId={}, amount={} {}",
                event.getOrderId(), event.getAmount(), event.getCurrency());
    }
}