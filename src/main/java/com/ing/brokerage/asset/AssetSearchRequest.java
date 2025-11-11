package com.ing.brokerage.asset;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetSearchRequest {

    @NotNull
    private UUID customerId;

    private String assetName;
}
