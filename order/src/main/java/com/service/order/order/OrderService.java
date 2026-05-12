package com.service.order.order;

import com.service.common.events.EventEnvelope;
import com.service.common.events.OrderCreatedPayload;
import com.service.common.events.OrderItemPayload;
import com.service.order.kafka.OrderEventProducer;
import com.service.order.order.dto.CreateOrderRequest;
import com.service.order.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        orderRepository.save(order);

        OrderCreatedPayload payload = OrderCreatedPayload.builder()
                .orderId(orderId.toString())
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .items(request.getItems().stream()
                        .map(item -> OrderItemPayload.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();

        EventEnvelope<OrderCreatedPayload> event = EventEnvelope.<OrderCreatedPayload>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .eventVersion(1)
                .aggregateId(orderId.toString())
                .aggregateType("ORDER")
                .occurredAt(Instant.now())
                .source("order-service")
                .payload(payload)
                .build();

        orderEventProducer.publishOrderCreated(event);

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }

    @Transactional
    public void markOrderConfirmed(String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

    @Transactional
    public void markOrderCancelled(String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

}
