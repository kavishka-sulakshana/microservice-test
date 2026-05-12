package com.service.order.kafka;

import com.service.common.events.EventEnvelope;
import com.service.common.events.PaymentCompletedPayload;
import com.service.common.events.PaymentFailedPayload;
import com.service.order.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(
            topics = "payments.v1",
            groupId = "order-service"
    )
    public void consume(EventEnvelope<?> event,
                        Acknowledgment acknowledgment) {

        log.info("Payment event received eventId={}, eventType={}",
                event.getEventId(),
                event.getEventType());

        if ("PaymentCompleted".equals(event.getEventType()) && event.getPayload() instanceof PaymentCompletedPayload payload) {
            orderService.markOrderConfirmed(payload.getOrderId());
        } else if ("PaymentFailed".equals(event.getEventType()) && event.getPayload() instanceof PaymentFailedPayload payload) {
            orderService.markOrderCancelled(payload.getOrderId());
        }

        acknowledgment.acknowledge();
    }
}
