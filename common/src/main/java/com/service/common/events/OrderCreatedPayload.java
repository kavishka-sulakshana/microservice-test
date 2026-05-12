package com.service.common.events;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedPayload {
    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private List<OrderItemPayload> items;
}
