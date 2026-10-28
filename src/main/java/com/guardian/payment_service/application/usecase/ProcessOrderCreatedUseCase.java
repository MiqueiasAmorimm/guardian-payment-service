package com.guardian.payment_service.application.usecase;

import com.guardian.payment_service.domain.model.Payment;
import com.guardian.payment_service.infrastructure.PaymentRepository;
import com.guardian.payment_service.infrastructure.event.OrderCreatedEvent;
import com.guardian.payment_service.infrastructure.event.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessOrderCreatedUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public ProcessOrderCreatedUseCase(PaymentRepository paymentRepository,
                                      PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public void execute(OrderCreatedEvent event) {
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for orderId={}, ignoring duplicate event", event.getOrderId());
            return;
        }
        Payment payment = new Payment(event.getOrderId(), event.getAmount(), event.getCurrency());
        payment.approve();
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishApproved(saved);
    }
}