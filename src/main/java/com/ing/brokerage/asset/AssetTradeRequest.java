package com.ing.brokerage.asset;

import com.ing.brokerage.order.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetTradeRequest {

    @NotNull
    private UUID customerId;

    @NotBlank
    private String assetName;

    @NotNull
    private BigDecimal price;

    @NotNull
    private BigDecimal size;

    @NotNull
    private OrderSide orderSide;
}
