package com.ing.brokerage.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {

    @NotNull
    private UUID customerId;

    @NotBlank
    @Size(min = 2, max = 4)
    private String assetName;

    @NotNull
    private OrderSide orderSide;

    @PositiveOrZero
    @NotNull
    private BigDecimal size;

    @Positive
    @NotNull
    private BigDecimal price;
}
