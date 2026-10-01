package com.guardian.payment_service.domain.model;

import java.util.Map;
import java.util.Set;

public class PaymentStatusTransitionValidator {
    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = Map.of(
    PaymentStatus.PENDING,  Set.of(PaymentStatus.APPROVED, PaymentStatus.REJECTED),
            PaymentStatus.APPROVED, Set.of(),
            PaymentStatus.REJECTED, Set.of()

    );
    public boolean canTransition(PaymentStatus current, PaymentStatus target) {
        Set<PaymentStatus> allowedTargets = ALLOWED_TRANSITIONS.get(current);
        return allowedTargets != null && allowedTargets.contains(target);
    }
}

