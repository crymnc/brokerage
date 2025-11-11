package com.ing.brokerage.config;

import static com.ing.brokerage.customer.Role.ADMIN;
import static com.ing.brokerage.customer.Role.CUSTOMER;

import com.ing.brokerage.customer.UserResponse;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean isSelfOrAdmin(Authentication authentication, UUID customerId) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        UserResponse user = (UserResponse) authentication.getPrincipal();

        return ADMIN.equals(user.getRole()) || (CUSTOMER.equals(user.getRole()) && user.getId().equals(customerId));
    }

}
