package com.ing.brokerage.customer;

import static com.ing.brokerage.customer.CustomerData.customerCreateRequest;
import static com.ing.brokerage.customer.CustomerData.customerEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() throws Exception {

        when(customerRepository.findCustomerByUsername("johndoe")).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customerEntity());

        CustomerResponse response = customerService.createCustomer(customerCreateRequest());

        assertThat(response).isNotNull();

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());

        CustomerEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getPassword()).isNotEqualTo("password123"); // Password should be encoded
        assertThat(savedEntity.getPassword()).isNotNull();
    }

    @Test
    void shouldThrowException_IfUsernameAlreadyExists_WhenCreateCustomer() {

        when(customerRepository.findCustomerByUsername("johndoe"))
            .thenReturn(Optional.of(customerEntity()));

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> customerService.createCustomer(customerCreateRequest()));
        Assertions.assertEquals(ExceptionConstants.USERNAME_ALREADY_EXISTS, ex.getMessageKey());

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldListCustomers() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerEntity> page = new PageImpl<>(List.of(customerEntity(), customerEntity()));

        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<CustomerResponse> result = customerService.listCustomers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(customerRepository).findAll(pageable);
    }

    @Test
    void shouldReturnEmptyPage_IfNoCustomers_WhenListCustomers() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerEntity> emptyPage = new PageImpl<>(List.of());

        when(customerRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<CustomerResponse> result = customerService.listCustomers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldGetCustomer() throws Exception {

        CustomerEntity customerEntity = customerEntity();
        when(customerRepository.findById(any())).thenReturn(Optional.of(customerEntity));

        CustomerResponse response = customerService.getCustomer(customerEntity.getId());

        assertThat(response).isNotNull();

        verify(customerRepository).findById(any());
    }

    @Test
    void shouldThrowException_IfCustomerNotFound_WhenGetCustomer() {

        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                       () ->  customerService.getCustomer(UUID.randomUUID()));
        Assertions.assertEquals(ExceptionConstants.CUSTOMER_NOT_FOUND, ex.getMessageKey());
    }

    @Test
    void shouldGetUserByUsername() {

        when(customerRepository.findCustomerByUsername(any()))
            .thenReturn(Optional.of(customerEntity()));

        Optional<UserResponse> result = customerService.getUserByUsername("johndoe");

        assertThat(result).isPresent();
        verify(customerRepository).findCustomerByUsername("johndoe");
    }

    @Test
    void shouldReturnEmpty_IfUserNotFound_WhenGetUserByUsername_() {

        when(customerRepository.findCustomerByUsername(any()))
            .thenReturn(Optional.empty());

        Optional<UserResponse> result = customerService.getUserByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckCustomerExists() {

        when(customerRepository.existsById(any())).thenReturn(true);

        boolean exists = customerService.customerExists(UUID.randomUUID());

        assertThat(exists).isTrue();
        verify(customerRepository).existsById(any());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {

        when(customerRepository.findById(any())).thenReturn(Optional.of(customerEntity()));

        customerService.deleteCustomer(UUID.randomUUID());

        verify(customerRepository).findById(any());
        verify(customerRepository).deleteById(any());
    }

    @Test
    void shouldThrowException_IfCustomerNotFound_WhenDeleteCustomer() {

        UUID nonExistentId = UUID.randomUUID();

        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                             () ->  customerService.deleteCustomer(nonExistentId));
        Assertions.assertEquals(ExceptionConstants.CUSTOMER_NOT_FOUND, ex.getMessageKey());

        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void updateCustomer_shouldUpdateCustomer_whenValidRequest() throws Exception {

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setName("John Updated");
        updateRequest.setSurname("Doe Updated");

        when(customerRepository.findById(any())).thenReturn(Optional.of(customerEntity()));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customerEntity());

        CustomerResponse response = customerService.updateCustomer(UUID.randomUUID(), updateRequest);

        assertThat(response).isNotNull();

        verify(customerRepository).findById(any());
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    void updateCustomer_shouldEncodePassword_whenPasswordProvided() throws Exception {

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setPassword("newPassword123");

        when(customerRepository.findById(any())).thenReturn(Optional.of(customerEntity()));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customerEntity());

        customerService.updateCustomer(UUID.randomUUID(), updateRequest);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());

        String encodedPassword = captor.getValue().getPassword();
        assertThat(encodedPassword).isNotEqualTo("newPassword123");
        assertThat(encodedPassword).startsWith("$2a$");
    }

    @Test
    void shouldThrowException_IfCustomerNotFound_WhenUpdateCustomer() {

        UUID nonExistentId = UUID.randomUUID();
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setName("John Updated");

        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                             () ->  customerService.updateCustomer(nonExistentId, updateRequest));
        Assertions.assertEquals(ExceptionConstants.CUSTOMER_NOT_FOUND, ex.getMessageKey());

        verify(customerRepository, never()).save(any());
    }
}
