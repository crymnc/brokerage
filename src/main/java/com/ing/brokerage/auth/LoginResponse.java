package com.ing.brokerage.auth;

import com.ing.brokerage.customer.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    private UUID id;

    private String name;

    private String surname;

    private String username;

    private Role role;
}
