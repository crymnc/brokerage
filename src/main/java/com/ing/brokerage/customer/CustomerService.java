package com.ing.brokerage.customer;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(rollbackFor = Exception.class)
    public CustomerResponse createCustomer(@Valid CustomerCreateRequest request) throws BusinessException {

        Optional<CustomerEntity> existingCustomer = customerRepository.findCustomerByUsername(request.getUsername());
        if (existingCustomer.isPresent()) {
            throw new BusinessException(ExceptionConstants.USERNAME_ALREADY_EXISTS);
        }
        CustomerEntity customerEntity = CustomerMapper.INSTANCE.toEntity(request);
        customerEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        return CustomerMapper.INSTANCE.toResponse(customerRepository.save(customerEntity));
    }

    public Page<CustomerResponse> listCustomers(Pageable pageable) {

        return customerRepository.findAll(pageable).map(CustomerMapper.INSTANCE::toResponse);
    }

    public CustomerResponse getCustomer(UUID customerId) throws RecordNotFoundException {

        return CustomerMapper.INSTANCE.toResponse(
            customerRepository.findById(customerId).orElseThrow(() -> new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND)));
    }

    public Optional<UserResponse> getUserByUsername(String username) {

        return customerRepository.findCustomerByUsername(username).map(CustomerMapper.INSTANCE::toUserResponse);
    }

    public boolean customerExists(UUID customerId) {

        return customerRepository.existsById(customerId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(UUID id) throws RecordNotFoundException {

        Optional<CustomerEntity> customerEntityOpt = customerRepository.findById(id);
        if (customerEntityOpt.isEmpty()) {

            throw new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND);
        }
        customerRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) throws RecordNotFoundException {

        Optional<CustomerEntity> customerEntityOpt = customerRepository.findById(id);
        if (customerEntityOpt.isEmpty()) {

            throw new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND);
        }
        CustomerEntity customerEntity = customerEntityOpt.get();
        CustomerMapper.INSTANCE.toEntity(customerEntity, request);
        if (StringUtils.isNotBlank(request.getPassword())) {
            customerEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return CustomerMapper.INSTANCE.toResponse(customerRepository.save(customerEntity));
    }
}
