package com.ing.brokerage.customer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdateRequest {

    private String name;

    private String surname;

    private String username;

    private String password;

    private Role role;
}
