package com.ing.brokerage.auth;

import com.ing.brokerage.config.JwtService;
import com.ing.brokerage.customer.CustomerService;
import com.ing.brokerage.customer.UserResponse;
import com.ing.brokerage.exception.NotAuthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class AuthenticationService {

    private final CustomerService customerService;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public LoginResponse login(@Valid LoginRequest request) throws NotAuthorizedException {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        UserResponse customer = customerService.getUserByUsername(request.getUsername())
                                               .orElseThrow(() -> new NotAuthorizedException(""));

        String jwtToken = jwtService.generateToken(customer);
        return AuthenticationMapper.INSTANCE.toResponse(jwtToken, customer);
    }
}
