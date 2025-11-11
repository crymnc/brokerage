package com.ing.brokerage.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSide {

    BUY(1L),
    SELL(2L);

    private final Long id;
}
