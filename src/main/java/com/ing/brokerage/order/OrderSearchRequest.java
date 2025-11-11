package com.ing.brokerage.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchRequest {

    @NotNull
    private UUID customerId;

    private String assetName;

    private OrderSide orderSide;

    private OrderStatus orderStatus;

    @NotNull
    private LocalDate startDate;

    @Positive
    @NotNull
    private Integer dateRange;
}
