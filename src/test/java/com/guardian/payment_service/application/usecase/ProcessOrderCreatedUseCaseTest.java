package com.guardian.payment_service.application.usecase;

import com.guardian.payment_service.domain.model.Payment;
import com.guardian.payment_service.infrastructure.PaymentRepository;
import com.guardian.payment_service.infrastructure.event.OrderCreatedEvent;
import com.guardian.payment_service.infrastructure.event.PaymentEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessOrderCreatedUseCaseTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private ProcessOrderCreatedUseCase processOrderCreatedUseCase;

    @Test
    void shouldIgnoreEventWhenPaymentAlreadyExists() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(UUID.randomUUID());
        event.setAmount(new BigDecimal("200.00"));
        event.setCurrency("BRL");
        when(paymentRepository.existsByOrderId(event.getOrderId())).thenReturn(true);

        processOrderCreatedUseCase.execute(event);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventPublisher, never()).publishApproved(any(Payment.class));
    }
}
