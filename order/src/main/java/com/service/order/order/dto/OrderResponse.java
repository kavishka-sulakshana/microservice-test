package com.service.order.order.dto;
import com.service.order.order.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private String userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}

//step 11