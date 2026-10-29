# Technical Decisions — Guardian Payment Service

This document records the key architectural and technical decisions made during the development of the payment-service.

## Own PaymentStatus Enum, Separate from OrderStatus

Payment status (PENDING, APPROVED, REJECTED) describes the payment process, while order status describes the order lifecycle. Each service owns its own state, so payment-service has its own enum instead of sharing the one from order-service. The name AWAITING_PAYMENT was deliberately not reused: it describes an order waiting for payment, not a payment being processed.

APPROVED and REJECTED are terminal states, enforced by a separate `PaymentStatusTransitionValidator` (same pattern as order-service), so a repeated or out-of-order decision cannot overwrite a final status. payment-service never writes to the orders table: it publishes events, and order-service decides whether to accept the change.

## Own Copy of OrderCreatedEvent

Considered importing the event class from order-service, a shared library, or a local copy. Chose a local copy. Importing from order-service would couple build and deploy between the two services. A shared library avoids duplication but brings back part of that version coupling.

With a local copy, the only link between the services is the contract (the JSON format). payment-service declares just the fields it needs, so new fields added by order-service later do not break it (tolerant reader). The trade-off is that field names must be kept in sync manually, and format details are part of the contract too: for example, `createdAt` travels as epoch seconds with a fractional part, which is how Jackson serializes `java.time.Instant` by default. There is no contract test that would catch a renamed field.

## Database per Service

payment-service owns its own PostgreSQL database (`guardian_payment`, port 5435), following the same pattern as catalog-service and order-service. No other service reads from or writes to it.

## Idempotent Consumer for order.created

Kafka guarantees at-least-once delivery, so the same `order.created` event can arrive twice (for example, after a restart before the offset is committed). Without protection, a repeated event would create a second payment and charge the order twice.

Two layers handle this. The use case asks `existsByOrderId` and ignores the event if a payment already exists, which covers the common case. A unique constraint on `payments.order_id` covers the rare case where two copies are processed at the same time, because the database rejects the second insert. Considered relying only on the constraint, but then the duplicate would surface as an exception instead of a controlled warning in the log.

Trade-offs: the check and the insert are not atomic, so the constraint is what actually guarantees uniqueness. The duplicate check also has a side effect: if the payment is saved but publishing `payment.approved` fails, a redelivery of `order.created` finds the existing payment and exits, so the approval is never sent again. The transactional outbox would fix both directions. The duplicate path is not covered by an automated test yet.

## Publishing Isolated in PaymentEventPublisher

`ProcessOrderCreatedUseCase` does not use `KafkaTemplate` directly. A `PaymentEventPublisher` (in `infrastructure.event`) receives the saved `Payment`, builds the `PaymentApprovedEvent` and sends it to `payment.approved`. The topic name and the Kafka API live in one class, and the use case only calls `publishApproved(...)` after the `save`.

Considered injecting `KafkaTemplate` into the use case, as order-service does in `CreateOrderUseCase`. It is simpler and has one class less, but the use case would know about Kafka. Chose the separate class so the business flow does not depend on the messaging mechanism and can be tested with a simple mock of the publisher.

Trade-offs: one more class and one more constructor argument. Order-service and payment-service now follow different styles for the same problem. The result of the Kafka send is not checked, so a failed publish is not detected. No automated test covers the publisher yet.

## Simulated Payment Approval

Every payment is approved. The service has no payment provider, card or customer, which is what decides approval in a real system, so there is nothing real to decide with.

Considered an amount limit above which payments are rejected, a random outcome, and always approving. The limit was dropped because it would look like a business rule of the store, when the real decision belongs to the payment provider; the order total is also already calculated by order-service, so checking it again here would mean consulting the catalog. A random outcome makes the flow hard to debug. Chose always approving, so the full cycle can be tested end to end.

Trade-offs: only the happy path exists. `Payment.reject(reason)` is implemented, but nothing triggers it, `payment.rejected` is not published and order-service does not handle it. The first real rule is the next thing to decide.