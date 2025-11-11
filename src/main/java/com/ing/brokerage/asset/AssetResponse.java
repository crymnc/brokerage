package com.ing.brokerage.asset;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetResponse {

    private Long id;

    private UUID customerId;

    private String assetName;

    private BigDecimal size;

    private BigDecimal usableSize;
}
