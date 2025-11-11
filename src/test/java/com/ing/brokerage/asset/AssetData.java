package com.ing.brokerage.asset;

import java.math.BigDecimal;
import java.util.UUID;

public class AssetData {

    public static AssetSearchRequest assetSearchRequest() {
        AssetSearchRequest request = new AssetSearchRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setAssetName("TEST");
        return request;
    }

    public static AssetEntity tryAssetEntity() {
        AssetEntity entity = new AssetEntity();
        entity.setId(1L);
        entity.setCustomerId(UUID.randomUUID());
        entity.setAssetName("TRY");
        entity.setSize(BigDecimal.valueOf(10000));
        entity.setUsableSize(BigDecimal.valueOf(5000));
        return entity;
    }

    public static AssetEntity assetEntity() {
        AssetEntity entity = new AssetEntity();
        entity.setId(2L);
        entity.setCustomerId(UUID.randomUUID());
        entity.setAssetName("TEST");
        entity.setSize(BigDecimal.valueOf(100));
        entity.setUsableSize(BigDecimal.valueOf(80));
        return entity;
    }

    public static AssetResponse assetResponse() {
        AssetResponse response = new AssetResponse();
        response.setId(1L);
        response.setCustomerId(UUID.randomUUID());
        response.setAssetName("TEST");
        response.setSize(BigDecimal.valueOf(100));
        response.setUsableSize(BigDecimal.valueOf(80));
        return response;
    }
}
