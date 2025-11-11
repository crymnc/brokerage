package com.ing.brokerage.asset;

import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

class AssetSpecification {

    public Specification<AssetEntity> customerIdEquals(UUID customerId) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), customerId);
    }

    public Specification<AssetEntity> assetNameEquals(String assetName) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assetName"), assetName);
    }

}
