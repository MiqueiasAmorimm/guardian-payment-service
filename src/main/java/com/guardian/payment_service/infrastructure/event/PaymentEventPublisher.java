package com.guardian.payment_service.infrastructure.event;

import com.guardian.payment_service.domain.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentApprovedEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentApprovedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishApproved(Payment payment) {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency());
        kafkaTemplate.send("payment.approved", event);
    }
}