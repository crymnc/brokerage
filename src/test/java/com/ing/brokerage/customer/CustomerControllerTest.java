package com.ing.brokerage.customer;

import static com.ing.brokerage.customer.CustomerData.customerCreateRequest;
import static com.ing.brokerage.customer.CustomerData.customerResponse;
import static com.ing.brokerage.customer.CustomerData.customerUpdateRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.brokerage.SecurityTestConfig;
import com.ing.brokerage.config.JwtService;
import com.ing.brokerage.config.UserSecurity;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@Import(SecurityTestConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    @Qualifier("customMessageResource")
    private MessageSource messageSource;

    @MockitoBean
    private UserSecurity userSecurity;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCustomer() throws Exception {

        CustomerResponse response = customerResponse();
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerCreateRequest())))
               .andExpect(status().isCreated())
               .andExpect(header().exists("Location"))
               .andExpect(jsonPath("$.id").value(response.getId().toString()))
               .andExpect(jsonPath("$.name").value(response.getName()))
               .andExpect(jsonPath("$.surname").value(response.getSurname()))
               .andExpect(jsonPath("$.username").value(response.getUsername()))
               .andExpect(jsonPath("$.role").value(response.getRole().name()));

        verify(customerService).createCustomer(any(CustomerCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnUnauthorized_IfNotAdmin_WhenCreateCustomer() throws Exception {

        mockMvc.perform(post("/v1/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerCreateRequest())))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnOk_IfAdmin_WhenListCustomers() throws Exception {

        when(customerService.listCustomers(any()))
            .thenReturn(new PageImpl<>(List.of(customerResponse(), customerResponse()), PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/v1/customers")
                            .param("page", "0")
                            .param("size", "10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(2));

        verify(customerService).listCustomers(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnUnauthorized_IfNotAdmin_WhenListCustomers() throws Exception {
        mockMvc.perform(get("/v1/customers"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnOk_IfSelfOrAdmin_WhenGetCustomer() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);

        CustomerResponse response = customerResponse();
        when(customerService.getCustomer(any())).thenReturn(response);

        mockMvc.perform(get("/v1/customers/{customerId}", response.getId().toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(response.getId().toString()))
               .andExpect(jsonPath("$.name").value("John"))
               .andExpect(jsonPath("$.surname").value("Doe"))
               .andExpect(jsonPath("$.username").value("johndoe"));

        verify(customerService).getCustomer(response.getId());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnUnauthorized_IfNotSelfOrAdmin_WhenGetCustomer() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(false);

        mockMvc.perform(get("/v1/customers/{customerId}", UUID.randomUUID()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnNotFound_IfCustomerNotExists_WhenGetCustomer() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(customerService.getCustomer(any()))
            .thenThrow(new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND));

        mockMvc.perform(get("/v1/customers/{customerId}", UUID.randomUUID()))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnOk_IfSelfOrAdmin_WhenUpdateCustomer() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(customerService.updateCustomer(any(), any(CustomerUpdateRequest.class)))
            .thenReturn(customerResponse());

        mockMvc.perform(patch("/v1/customers/{customerId}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerUpdateRequest())))
               .andExpect(status().isOk());

        verify(customerService).updateCustomer(any(), any(CustomerUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_IfNotSelfOrAdmin_WhenUpdateCustomer() throws Exception {

        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setName("John Updated");

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(false);

        mockMvc.perform(patch("/v1/customers/{customerId}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnNotFound_IfCustomerNotExists_WhenUpdateCustomer() throws Exception {

        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setName("John Updated");

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(customerService.updateCustomer(any(), any(CustomerUpdateRequest.class)))
            .thenThrow(new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND));

        mockMvc.perform(patch("/v1/customers/{customerId}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNoContent_IfAdmin_WhenDeleteCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();

        doNothing().when(customerService).deleteCustomer(any());

        mockMvc.perform(delete("/v1/customers/{customerId}", customerId)
                            .with(csrf()))
               .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFound_IfCustomerNotExists_WhenDeleteCustomer() throws Exception {

        doThrow(new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND))
            .when(customerService).deleteCustomer(any());

        mockMvc.perform(delete("/v1/customers/{customerId}", UUID.randomUUID())
                            .with(csrf()))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnUnauthorized_IfNotAdmin_WhenDeleteCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(delete("/v1/customers/{customerId}", customerId)
                            .with(csrf()))
               .andExpect(status().isUnauthorized());
    }
}
