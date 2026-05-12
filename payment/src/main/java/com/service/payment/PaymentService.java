package com.service.payment;

import com.service.common.events.EventEnvelope;
import com.service.common.events.OrderCreatedPayload;
import com.service.common.events.PaymentCompletedPayload;
import com.service.common.events.PaymentFailedPayload;
import com.service.payment.kafka.PaymentEventProducer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public void processPayment(EventEnvelope<OrderCreatedPayload> orderEvent) {
        UUID paymentId = UUID.randomUUID();

        OrderCreatedPayload order = orderEvent.getPayload();
        boolean paymentApproved = order.getTotalAmount() != null
                && order.getTotalAmount().signum() > 0;

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .status(paymentApproved ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        if (!paymentApproved) {
            PaymentFailedPayload payload = PaymentFailedPayload.builder()
                    .paymentId(paymentId.toString())
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .amount(order.getTotalAmount())
                    .reason("Payment amount must be greater than zero")
                    .status("FAILED")
                    .build();

            EventEnvelope<PaymentFailedPayload> event = EventEnvelope.<PaymentFailedPayload>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PaymentFailed")
                    .eventVersion(1)
                    .aggregateId(order.getOrderId())
                    .aggregateType("PAYMENT")
                    .occurredAt(Instant.now())
                    .source("payment-service")
                    .payload(payload)
                    .build();

            paymentEventProducer.publishPaymentFailed(event);
            return;
        }

        PaymentCompletedPayload payload = PaymentCompletedPayload.builder()
                .paymentId(paymentId.toString())
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .status("COMPLETED")
                .build();

        EventEnvelope<PaymentCompletedPayload> event = EventEnvelope.<PaymentCompletedPayload>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PaymentCompleted")
                .eventVersion(1)
                .aggregateId(order.getOrderId())
                .aggregateType("PAYMENT")
                .occurredAt(Instant.now())
                .source("payment-service")
                .payload(payload)
                .build();

        paymentEventProducer.publishPaymentCompleted(event);
    }

    @Transactional
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
