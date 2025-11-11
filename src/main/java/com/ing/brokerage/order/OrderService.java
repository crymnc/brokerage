package com.ing.brokerage.order;

import static com.ing.brokerage.constant.Constants.TRY_ASSET;

import com.ing.brokerage.asset.AssetResponse;
import com.ing.brokerage.asset.AssetService;
import com.ing.brokerage.customer.CustomerService;
import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.exception.RecordNotFoundException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderService {

    private final OrderRepository orderRepository;

    private final CustomerService customerService;

    private final AssetService assetService;

    private final OrderSpecification orderSpecification = new OrderSpecification();

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(@Valid OrderCreateRequest request) throws RecordNotFoundException, BusinessException {

        if (TRY_ASSET.equals(request.getAssetName()) && request.getPrice().compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(ExceptionConstants.TRY_ORDER_PRICE_MUST_BE_ONE);
        }

        if (!customerService.customerExists(request.getCustomerId())) {
            throw new RecordNotFoundException(ExceptionConstants.CUSTOMER_NOT_FOUND);
        }

        AssetResponse asset = null;
        if (!TRY_ASSET.equals(request.getAssetName())) {
            if (OrderSide.BUY.equals(request.getOrderSide())) {
                asset = assetService.lockAsset(request.getCustomerId(), TRY_ASSET, request.getPrice().multiply(request.getSize()));
            } else {
                asset = assetService.lockAsset(request.getCustomerId(), request.getAssetName(), request.getSize());
            }
        }

        OrderEntity orderEntity = OrderMapper.INSTANCE.toEntity(request);

        return OrderMapper.INSTANCE.toResponse(orderRepository.save(orderEntity), asset);
    }

    public Page<OrderResponse> listOrders(@Valid OrderSearchRequest searchRequest, Pageable pageable) {

        Specification<OrderEntity> spc = generateSearchParametersAsSpecification(searchRequest);
        return orderRepository.findAll(spc, pageable)
                              .map(order -> OrderMapper.INSTANCE.toResponse(order, null));
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse matchOrder(Long orderId) throws RecordNotFoundException, BusinessException {

        Optional<OrderEntity> orderEntityOpt = orderRepository.findById(orderId);

        if (orderEntityOpt.isEmpty()) {
            throw new RecordNotFoundException(ExceptionConstants.ORDER_NOT_FOUND);
        }

        OrderEntity orderEntity = orderEntityOpt.get();

        if (!OrderStatus.PENDING.equals(orderEntity.getOrderStatus())) {
            throw new BusinessException(ExceptionConstants.ORDER_STATUS_NOT_PENDING);
        }

        AssetResponse asset = assetService.matchTradingAsset(orderEntity.getCustomerId(), orderEntity.getAssetName(),
                                                             orderEntity.getPrice(),
                                                             orderEntity.getSize(), orderEntity.getOrderSide());

        orderEntity.setOrderStatus(OrderStatus.MATCHED);
        return OrderMapper.INSTANCE.toResponse(orderRepository.save(orderEntity), asset);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long orderId) throws BusinessException, RecordNotFoundException {

        Optional<OrderEntity> orderEntityOpt = orderRepository.findById(orderId);

        if (orderEntityOpt.isEmpty()) {
            throw new RecordNotFoundException(ExceptionConstants.ORDER_NOT_FOUND);
        }

        OrderEntity orderEntity = orderEntityOpt.get();

        if (!OrderStatus.PENDING.equals(orderEntity.getOrderStatus())) {
            throw new BusinessException(ExceptionConstants.ORDER_STATUS_NOT_PENDING);
        }

        AssetResponse asset;
        if (OrderSide.BUY.equals(orderEntity.getOrderSide())) {
            asset = assetService.unlockAsset(orderEntity.getCustomerId(), TRY_ASSET,
                                             orderEntity.getPrice().multiply(orderEntity.getSize()));
        } else {
            asset = assetService.unlockAsset(orderEntity.getCustomerId(), orderEntity.getAssetName(), orderEntity.getSize());
        }
        orderEntity.setOrderStatus(OrderStatus.CANCELLED);
        return OrderMapper.INSTANCE.toResponse(orderRepository.save(orderEntity), asset);
    }

    private Specification<OrderEntity> generateSearchParametersAsSpecification(OrderSearchRequest searchRequest) {

        Specification<OrderEntity> spc =
            orderSpecification.customerIdEquals(searchRequest.getCustomerId())
                              .and(orderSpecification.createdAtInRangeEquals(searchRequest.getStartDate().atStartOfDay().atOffset(
                                                                                 ZoneOffset.UTC),
                                                                             searchRequest.getDateRange()));

        if (searchRequest.getOrderSide() != null) {
            spc = orderSpecification.orderSideEquals(searchRequest.getOrderSide());
        }
        if (searchRequest.getOrderStatus() != null) {
            spc = orderSpecification.orderStatusEquals(searchRequest.getOrderStatus());
        }
        if (searchRequest.getAssetName() != null) {
            spc = orderSpecification.assetNameEquals(searchRequest.getAssetName());
        }
        return spc;
    }


}
