package com.ing.brokerage.order;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

class OrderSpecification {

    public Specification<OrderEntity> customerIdEquals(UUID customerId) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), customerId);
    }

    public Specification<OrderEntity> createdAtInRangeEquals(OffsetDateTime startDate, Integer dateRange) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("createdAt"), startDate, startDate.plusDays(dateRange));
    }

    public Specification<OrderEntity> orderStatusEquals(OrderStatus orderStatus) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("orderStatus"), orderStatus);
    }

    public Specification<OrderEntity> orderSideEquals(OrderSide orderSide) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("orderSide"), orderSide);
    }

    public Specification<OrderEntity> assetNameEquals(String assetName) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assetName"), assetName);
    }

}
