package com.service.order.order.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequest {
    @NotBlank
    private String productId;

    @Min(1)
    private Integer quantity;
}
