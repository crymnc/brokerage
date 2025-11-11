package com.ing.brokerage.customer;

import java.util.UUID;

public class CustomerData {

    public static CustomerCreateRequest customerCreateRequest() {

        CustomerCreateRequest createRequest = new CustomerCreateRequest();
        createRequest.setName("John");
        createRequest.setSurname("Doe");
        createRequest.setUsername("johndoe");
        createRequest.setPassword("password123");
        createRequest.setRole(Role.CUSTOMER);
        return createRequest;
    }

    public static CustomerEntity customerEntity() {

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(UUID.randomUUID());
        customerEntity.setName("John");
        customerEntity.setSurname("Doe");
        customerEntity.setUsername("johndoe");
        customerEntity.setPassword("encodedPassword");
        customerEntity.setRole(Role.CUSTOMER);
        return customerEntity;
    }

    public static CustomerResponse customerResponse() {

        CustomerResponse response = new CustomerResponse();
        response.setId(UUID.randomUUID());
        response.setName("John");
        response.setSurname("Doe");
        response.setUsername("johndoe");
        response.setRole(Role.CUSTOMER);
        return response;
    }

    public static CustomerUpdateRequest customerUpdateRequest() {

        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setName("John Updated");
        request.setSurname("Doe Updated");
        return request;
    }
}
