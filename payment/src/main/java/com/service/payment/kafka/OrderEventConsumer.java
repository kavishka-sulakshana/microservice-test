package com.service.payment.kafka;

import com.service.common.events.EventEnvelope;
import com.service.common.events.OrderCreatedPayload;
import com.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "orders.v1",
            groupId = "payment-service"
    )
    public void consume(EventEnvelope<OrderCreatedPayload> event,
                        Acknowledgment acknowledgment) {

        log.info("Order event received eventId={}, eventType={}",
                event.getEventId(),
                event.getEventType());

        if ("OrderCreated".equals(event.getEventType())) {
            paymentService.processPayment(event);
        }

        acknowledgment.acknowledge();
    }
}
