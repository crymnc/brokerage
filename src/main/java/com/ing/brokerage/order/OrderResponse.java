package com.ing.brokerage.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {

    private Long id;

    private UUID customerId;

    private String assetName;

    private OrderSide orderSide;

    private BigDecimal size;

    private BigDecimal price;

    private OrderStatus orderStatus;

    private BigDecimal assetSize;

    private BigDecimal assetUsableSize;

}
