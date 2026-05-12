package com.service.payment;

import com.service.common.events.EventEnvelope;
import com.service.common.events.OrderCreatedPayload;
import com.service.payment.kafka.PaymentEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentPublishesCompletedEventForPositiveAmount() {
        EventEnvelope<OrderCreatedPayload> orderEvent = EventEnvelope.<OrderCreatedPayload>builder()
                .payload(OrderCreatedPayload.builder()
                        .orderId("order-1")
                        .userId("user-1")
                        .totalAmount(new BigDecimal("25.00"))
                        .build())
                .build();

        paymentService.processPayment(orderEvent);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentEventProducer).publishPaymentCompleted(any());
        verify(paymentEventProducer, never()).publishPaymentFailed(any());
    }

    @Test
    void processPaymentPublishesFailedEventForNonPositiveAmount() {
        EventEnvelope<OrderCreatedPayload> orderEvent = EventEnvelope.<OrderCreatedPayload>builder()
                .payload(OrderCreatedPayload.builder()
                        .orderId("order-2")
                        .userId("user-2")
                        .totalAmount(BigDecimal.ZERO)
                        .build())
                .build();

        paymentService.processPayment(orderEvent);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentEventProducer).publishPaymentFailed(any());
        verify(paymentEventProducer, never()).publishPaymentCompleted(any());
    }
}
