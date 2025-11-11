package com.ing.brokerage.customer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN(1L),
    CUSTOMER(2L);

    private final Long id;
}
