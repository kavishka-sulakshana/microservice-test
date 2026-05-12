package com.service.common.events;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPayload {
    private String productId;
    private Integer quantity;
}
