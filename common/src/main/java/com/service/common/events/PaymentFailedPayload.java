package com.service.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedPayload {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String reason;
    private String status;
}
