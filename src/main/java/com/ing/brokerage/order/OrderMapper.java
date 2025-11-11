package com.ing.brokerage.order;

import com.ing.brokerage.asset.AssetResponse;
import com.ing.brokerage.asset.AssetTradeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "orderStatus", constant = "PENDING")
    OrderEntity toEntity(OrderCreateRequest request);

    @Mapping(target = "assetSize", source = "asset.size")
    @Mapping(target = "assetUsableSize", source = "asset.usableSize")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "assetName", source = "entity.assetName")
    @Mapping(target = "size", source = "entity.size")
    @Mapping(target = "customerId", source = "entity.customerId")
    OrderResponse toResponse(OrderEntity entity, AssetResponse asset);

    AssetTradeRequest toRequest(OrderCreateRequest request);
}
