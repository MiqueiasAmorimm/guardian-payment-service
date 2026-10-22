package com.guardian.payment_service.application.usecase;

import com.guardian.payment_service.domain.model.Payment;
import com.guardian.payment_service.infrastructure.PaymentRepository;
import com.guardian.payment_service.infrastructure.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessOrderCreatedUseCase {

    private final PaymentRepository paymentRepository;

    public ProcessOrderCreatedUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;

    }
    public void execute(OrderCreatedEvent event) {
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for orderId={}, ignoring duplicate event", event.getOrderId());
            return;
        }
        Payment payment = new Payment(event.getOrderId(), event.getAmount(), event.getCurrency());
        payment.approve();
        paymentRepository.save(payment);
    }
}