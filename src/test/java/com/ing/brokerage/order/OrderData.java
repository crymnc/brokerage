package com.ing.brokerage.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class OrderData {

    public static OrderEntity orderEntity() {

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setCustomerId(UUID.randomUUID());
        orderEntity.setAssetName("AAPL");
        orderEntity.setOrderSide(OrderSide.BUY);
        orderEntity.setSize(BigDecimal.TEN);
        orderEntity.setPrice(BigDecimal.valueOf(150));
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        return orderEntity;
    }

    public static OrderCreateRequest orderCreateRequest() {

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setCustomerId(UUID.randomUUID());
        createRequest.setAssetName("AAPL");
        createRequest.setOrderSide(OrderSide.BUY);
        createRequest.setSize(BigDecimal.TEN);
        createRequest.setPrice(BigDecimal.valueOf(150));
        return createRequest;
    }

    public static OrderSearchRequest orderSearchRequest() {

        OrderSearchRequest searchRequest = new OrderSearchRequest();
        searchRequest.setCustomerId(UUID.randomUUID());
        searchRequest.setStartDate(LocalDate.now());
        searchRequest.setDateRange(30);
        searchRequest.setAssetName("AAPL");
        searchRequest.setOrderSide(OrderSide.BUY);
        searchRequest.setOrderStatus(OrderStatus.PENDING);
        return searchRequest;
    }

    public static OrderResponse orderResponse() {

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setCustomerId(UUID.randomUUID());
        response.setAssetName("AAPL");
        response.setOrderSide(OrderSide.BUY);
        response.setSize(BigDecimal.TEN);
        response.setPrice(BigDecimal.valueOf(150));
        response.setOrderStatus(OrderStatus.PENDING);
        return response;
    }
}
