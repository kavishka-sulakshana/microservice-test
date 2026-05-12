package com.service.payment.kafka;

import com.service.common.events.EventEnvelope;
import com.service.common.events.PaymentCompletedPayload;
import com.service.common.events.PaymentFailedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(EventEnvelope<PaymentCompletedPayload> event) {
        kafkaTemplate.send("payments.v1", event.getAggregateId(), event);
    }

    public void publishPaymentFailed(EventEnvelope<PaymentFailedPayload> event) {
        kafkaTemplate.send("payments.v1", event.getAggregateId(), event);
    }

}
