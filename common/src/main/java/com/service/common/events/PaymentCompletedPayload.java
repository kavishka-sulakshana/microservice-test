package com.service.common.events;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedPayload {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
}
