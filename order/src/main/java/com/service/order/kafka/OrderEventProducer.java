package com.service.order.kafka;

import com.service.common.events.EventEnvelope;
import com.service.common.events.OrderCreatedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(EventEnvelope<OrderCreatedPayload> event) {
        kafkaTemplate.send("orders.v1", event.getAggregateId(), event);
    }
}
