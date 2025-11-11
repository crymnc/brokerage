package com.ing.brokerage.auth;

import com.ing.brokerage.customer.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthenticationMapper {

    AuthenticationMapper INSTANCE = Mappers.getMapper(AuthenticationMapper.class);

    LoginResponse toResponse(String token, UserResponse user);
}
