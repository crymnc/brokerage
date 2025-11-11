package com.ing.brokerage.order;

import static com.ing.brokerage.asset.AssetData.assetResponse;
import static com.ing.brokerage.constant.Constants.TRY_ASSET;
import static com.ing.brokerage.order.OrderData.orderCreateRequest;
import static com.ing.brokerage.order.OrderData.orderEntity;
import static com.ing.brokerage.order.OrderData.orderSearchRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ing.brokerage.asset.AssetService;
import com.ing.brokerage.customer.CustomerService;
import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import java.math.BigDecimal;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateBuyOrder() throws Exception {

        when(customerService.customerExists(any())).thenReturn(true);
        when(assetService.lockAsset(any(), eq(TRY_ASSET), any(BigDecimal.class)))
            .thenReturn(assetResponse());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity());

        OrderResponse response = orderService.createOrder(orderCreateRequest());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getAssetSize()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getAssetUsableSize()).isEqualTo(BigDecimal.valueOf(80));

        verify(customerService).customerExists(any());
        verify(assetService).lockAsset(any(), eq(TRY_ASSET), eq(BigDecimal.valueOf(1500)));
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void shouldCreateSellOrder() throws Exception {

        OrderCreateRequest orderCreateRequest = orderCreateRequest();
        orderCreateRequest.setOrderSide(OrderSide.SELL);

        when(customerService.customerExists(any())).thenReturn(true);
        when(assetService.lockAsset(any(), eq("AAPL"), eq(BigDecimal.TEN)))
            .thenReturn(assetResponse());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity());

        OrderResponse response = orderService.createOrder(orderCreateRequest);

        assertThat(response).isNotNull();
        verify(assetService).lockAsset(any(), eq("AAPL"), eq(BigDecimal.TEN));
    }

    @Test
    void shouldCreateTRYOrder_WhenAssetNameIsTRY() throws Exception {

        OrderCreateRequest orderCreateRequest = orderCreateRequest();
        orderCreateRequest.setAssetName(TRY_ASSET);
        orderCreateRequest.setPrice(BigDecimal.ONE);

        when(customerService.customerExists(any())).thenReturn(true);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity());

        OrderResponse response = orderService.createOrder(orderCreateRequest);

        assertThat(response).isNotNull();
        verify(assetService, never()).lockAsset(any(), any(), any());
    }

    @Test
    void shouldThrowException_IfTRYPriceNotOne_WhenCreateOrder() {

        OrderCreateRequest orderCreateRequest = orderCreateRequest();
        orderCreateRequest.setAssetName(TRY_ASSET);
        orderCreateRequest.setPrice(BigDecimal.valueOf(2));

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> orderService.createOrder(orderCreateRequest));
        Assertions.assertEquals(ExceptionConstants.TRY_ORDER_PRICE_MUST_BE_ONE, ex.getMessageKey());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_IfCustomerNotFound_WhenCreateOrder() throws BusinessException {

        when(customerService.customerExists(any())).thenReturn(false);

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                             () -> orderService.createOrder(orderCreateRequest()));
        Assertions.assertEquals(ExceptionConstants.CUSTOMER_NOT_FOUND, ex.getMessageKey());

        verify(assetService, never()).lockAsset(any(), any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldListOrders() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = new PageImpl<>(List.of(orderEntity()));

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrderResponse> result = orderService.listOrders(orderSearchRequest(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPage_IfNoOrdersFound_WhenListOrders() {


        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> emptyPage = new PageImpl<>(List.of());

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<OrderResponse> result = orderService.listOrders(orderSearchRequest(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldMatchOrder() throws Exception {

        OrderEntity orderEntity = orderEntity();
        orderEntity.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
        when(assetService.matchTradingAsset(
            any(),
            eq("AAPL"),
            eq(BigDecimal.valueOf(150)),
            eq(BigDecimal.TEN),
            eq(OrderSide.BUY)
        )).thenReturn(assetResponse());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        OrderResponse response = orderService.matchOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderStatus()).isEqualTo(OrderStatus.MATCHED);
    }

    @Test
    void shouldThrowException_IfOrderNotFound_WhenMatchOrder() throws BusinessException {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                             () -> orderService.matchOrder(999L));
        Assertions.assertEquals(ExceptionConstants.ORDER_NOT_FOUND, ex.getMessageKey());

        verify(assetService, never()).matchTradingAsset(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowException_IfOrderNotPending_WhenMatchOrder() throws BusinessException {

        OrderEntity orderEntity = orderEntity();
        orderEntity.setOrderStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> orderService.matchOrder(1L));
        Assertions.assertEquals(ExceptionConstants.ORDER_STATUS_NOT_PENDING, ex.getMessageKey());

        verify(assetService, never()).matchTradingAsset(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCancelBuyOrder() throws Exception {

        OrderEntity orderEntity = orderEntity();
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.setOrderSide(OrderSide.BUY);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
        when(assetService.unlockAsset(any(), eq(TRY_ASSET), any(BigDecimal.class)))
            .thenReturn(assetResponse());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        OrderResponse response = orderService.cancelOrder(1L);

        assertThat(response).isNotNull();

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(assetService).unlockAsset(any(), eq(TRY_ASSET), eq(BigDecimal.valueOf(1500)));
    }

    @Test
    void shouldCancelSellOrder() throws Exception {

        OrderEntity orderEntity = orderEntity();
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.setOrderSide(OrderSide.SELL);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
        when(assetService.unlockAsset(any(), eq("AAPL"), eq(BigDecimal.TEN)))
            .thenReturn(assetResponse());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        OrderResponse response = orderService.cancelOrder(1L);

        assertThat(response).isNotNull();
        verify(assetService).unlockAsset(any(), eq("AAPL"), eq(BigDecimal.TEN));
    }

    @Test
    void shouldThrowException_IfOrderNotFound_WhenCancelOrder() throws BusinessException {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RecordNotFoundException ex = Assertions.assertThrows(RecordNotFoundException.class,
                                                             () -> orderService.cancelOrder(999L));
        Assertions.assertEquals(ExceptionConstants.ORDER_NOT_FOUND, ex.getMessageKey());

        verify(assetService, never()).unlockAsset(any(), any(), any());
    }

    @Test
    void shouldThrowException_IfOrderNotPending_WhenCancelOrder() throws BusinessException {

        OrderEntity orderEntity = orderEntity();
        orderEntity.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> orderService.cancelOrder(1L));
        Assertions.assertEquals(ExceptionConstants.ORDER_STATUS_NOT_PENDING, ex.getMessageKey());

        verify(assetService, never()).unlockAsset(any(), any(), any());
    }
}
