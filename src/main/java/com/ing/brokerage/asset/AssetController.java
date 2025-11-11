package com.ing.brokerage.asset;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assets")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize(value = "@userSecurity.isSelfOrAdmin(authentication, #searchRequest.customerId)")
    public ResponseEntity<Page<AssetResponse>> listAssets(
        @ParameterObject AssetSearchRequest searchRequest,
        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(assetService.listAssets(searchRequest, pageable));
    }

}
