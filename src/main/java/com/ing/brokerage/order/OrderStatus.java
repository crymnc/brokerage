package com.ing.brokerage.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING(1L),
    MATCHED(2L),
    CANCELLED(3L);

    private final Long id;
}
