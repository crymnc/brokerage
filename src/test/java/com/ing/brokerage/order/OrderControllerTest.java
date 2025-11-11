package com.ing.brokerage.order;
import static com.ing.brokerage.order.OrderData.orderCreateRequest;
import static com.ing.brokerage.order.OrderData.orderResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.brokerage.SecurityTestConfig;
import com.ing.brokerage.config.JwtService;
import com.ing.brokerage.config.UserSecurity;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

@WebMvcTest(controllers = OrderController.class)
@Import({SecurityTestConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    @Qualifier("customMessageResource")
    private MessageSource messageSource;

    @MockitoBean
    private UserSecurity userSecurity;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateOrder() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        OrderCreateRequest request = orderCreateRequest();

        OrderResponse response = orderResponse();
        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.customerId").value(response.getCustomerId().toString()))
               .andExpect(jsonPath("$.assetName").value(response.getAssetName()))
               .andExpect(jsonPath("$.orderSide").value(response.getOrderSide().name()))
               .andExpect(jsonPath("$.orderStatus").value(response.getOrderStatus().name()));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnBadRequest_IfCustomerNotFound_WhenCreateOrder() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        when(orderService.createOrder(any(OrderCreateRequest.class)))
            .thenThrow(new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND));

        mockMvc.perform(post("/v1/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderCreateRequest())))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldMatchOrder() throws Exception {

        Long orderId = 1L;
        OrderResponse response = orderResponse();
        response.setId(orderId);
        response.setOrderStatus(OrderStatus.MATCHED);

        when(orderService.matchOrder(orderId)).thenReturn(response);

        mockMvc.perform(patch("/v1/orders/{orderId}", orderId)
                            .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(orderId))
               .andExpect(jsonPath("$.orderStatus").value("MATCHED"));

        verify(orderService).matchOrder(orderId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldUnauthorized_IfNotAdmin_WhenMatchOrder() throws Exception {

        mockMvc.perform(patch("/v1/orders/{orderId}", 1L)
                            .with(csrf()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCancelOrder() throws Exception {

        Long orderId = 1L;
        OrderResponse response = orderResponse();
        response.setId(orderId);
        response.setOrderStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(orderId)).thenReturn(response);

        mockMvc.perform(delete("/v1/orders/{orderId}", orderId)
                            .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(orderId))
               .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));

        verify(orderService).cancelOrder(orderId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFound_IfOrderNotExists_WhenCancelOrder() throws Exception {

        Long orderId = 999L;

        when(orderService.cancelOrder(orderId))
            .thenThrow(new RecordNotFoundException(ExceptionConstants.ORDER_NOT_FOUND));

        mockMvc.perform(delete("/v1/orders/{orderId}", orderId)
                            .with(csrf()))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnUnauthorized_IfNotAdmin_WhenCancelOrder() throws Exception {

        mockMvc.perform(delete("/v1/orders/{orderId}", 1L)
                            .with(csrf()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldListOrders() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);
        OrderResponse order = orderResponse();
        when(orderService.listOrders(any(OrderSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/v1/orders")
                            .param("customerId", order.getCustomerId().toString())
                            .param("startDate", LocalDate.now().toString())
                            .param("dateRange", "30")
                            .param("page", "0")
                            .param("size", "10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].id").value(1))
               .andExpect(jsonPath("$.content[0].customerId").value(order.getCustomerId().toString()))
               .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderService).listOrders(any(OrderSearchRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnOk_IfAdminRequestsAnyCustomer_WhenListOrders() throws Exception {

        when(userSecurity.isSelfOrAdmin(any(), any())).thenReturn(true);

        UUID customerId = UUID.randomUUID();
        OrderResponse response = new OrderResponse();
        response.setId(1L);

        when(orderService.listOrders(any(OrderSearchRequest.class), any()))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/v1/orders")
                            .param("customerId", customerId.toString())
                            .param("startDate", LocalDate.now().toString())
                            .param("dateRange", "30"))
               .andExpect(status().isOk());
    }
}
