# Technical Decisions — Guardian Payment Service

This document records the key architectural and technical decisions made during the development of the payment-service.

## Own PaymentStatus Enum, Separate from OrderStatus

Payment status (PENDING, APPROVED, REJECTED) describes the payment process, while order status describes the order lifecycle. Each service owns its own state, so payment-service has its own enum instead of sharing the one from order-service. The name AWAITING_PAYMENT was deliberately not reused: it describes an order waiting for payment, not a payment being processed.

APPROVED and REJECTED are terminal states, enforced by a separate `PaymentStatusTransitionValidator` (same pattern as order-service), so a repeated or out-of-order decision cannot overwrite a final status. payment-service never writes to the orders table: it publishes events, and order-service decides whether to accept the change.

## Own Copy of OrderCreatedEvent

Considered importing the event class from order-service, a shared library, or a local copy. Chose a local copy. Importing from order-service would couple build and deploy between the two services. A shared library avoids duplication but brings back part of that version coupling.

With a local copy, the only link between the services is the contract (the JSON format). payment-service declares just the fields it needs, so new fields added by order-service later do not break it (tolerant reader). The trade-off is that field names must be kept in sync manually.

## Database per Service

payment-service owns its own PostgreSQL database (`guardian_payment`, port 5435), following the same pattern as catalog-service and order-service. No other service reads from or writes to it.