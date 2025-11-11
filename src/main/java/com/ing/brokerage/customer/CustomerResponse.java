package com.ing.brokerage.customer;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse  {

    private UUID id;

    private String name;

    private String surname;

    private String username;

    private Role role;

}
