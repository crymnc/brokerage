package com.ing.brokerage.asset;

import static com.ing.brokerage.constant.Constants.TRY_ASSET;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.order.OrderSide;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetSpecification assetSpecification = new AssetSpecification();

    public Page<AssetResponse> listAssets(@Valid AssetSearchRequest searchRequest, Pageable pageable) {

        Specification<AssetEntity> spc = generateSearchParametersAsSpecification(searchRequest);
        return assetRepository.findAll(spc, pageable)
                              .map(AssetMapper.INSTANCE::toResponse);
    }

    private Specification<AssetEntity> generateSearchParametersAsSpecification(AssetSearchRequest searchRequest) {

        Specification<AssetEntity> spc = assetSpecification.customerIdEquals(searchRequest.getCustomerId());
        if (StringUtils.isNotBlank(searchRequest.getAssetName())) {
            spc = assetSpecification.assetNameEquals(searchRequest.getAssetName());
        }
        return spc;
    }

    @Transactional(rollbackFor = Exception.class)
    public AssetResponse lockAsset(UUID customerId, String assetName, BigDecimal size) throws BusinessException {

        Optional<AssetEntity> assetOpt = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, assetName);

        if (assetOpt.isEmpty() || assetOpt.get().getUsableSize().compareTo(size) < 0) {
            throw new BusinessException(ExceptionConstants.INSUFFICIENT_ASSET, assetName);
        }

        AssetEntity assetEntity = assetOpt.get();
        assetEntity.setUsableSize(assetEntity.getUsableSize().subtract(size));

        return AssetMapper.INSTANCE.toResponse(assetRepository.save(assetEntity));
    }

    @Transactional(rollbackFor = Exception.class)
    public AssetResponse unlockAsset(UUID customerId, String assetName, BigDecimal size) throws BusinessException {

        Optional<AssetEntity> assetOpt = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, assetName);

        if (assetOpt.isEmpty()) {
            throw new BusinessException(ExceptionConstants.NO_ASSET_TO_UNLOCK);
        }

        AssetEntity assetEntity = assetOpt.get();
        assetEntity.setUsableSize(assetEntity.getUsableSize().add(size));
        return AssetMapper.INSTANCE.toResponse(assetRepository.save(assetEntity));
    }

    @Transactional(rollbackFor = Exception.class)
    public AssetResponse matchTradingAsset(UUID customerId, String assetName, BigDecimal price, BigDecimal size, OrderSide orderSide)
        throws BusinessException {

        return OrderSide.BUY.equals(orderSide)
            ? matchBuyingAsset(customerId, assetName, price, size)
            : matchSellAsset(customerId, assetName, price, size);
    }

    private AssetResponse matchBuyingAsset(UUID customerId, String assetName, BigDecimal price, BigDecimal size) throws BusinessException {

        if (!TRY_ASSET.equals(assetName)) {
            matchSellAsset(customerId, TRY_ASSET, price, size);
        }

        Optional<AssetEntity> assetOpt = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, assetName);

        BigDecimal calculatedSize = TRY_ASSET.equals(assetName) ? price.multiply(size) : size;
        AssetEntity assetEntity;
        if (assetOpt.isPresent()) {
            assetEntity = assetOpt.get();
            assetEntity.setUsableSize(assetEntity.getUsableSize().add(calculatedSize));
            assetEntity.setSize(assetEntity.getSize().add(calculatedSize));
        } else {
            assetEntity = new AssetEntity();
            assetEntity.setCustomerId(customerId);
            assetEntity.setAssetName(assetName);
            assetEntity.setSize(calculatedSize);
            assetEntity.setUsableSize(calculatedSize);
        }

        return AssetMapper.INSTANCE.toResponse(assetRepository.save(assetEntity));
    }

    private AssetResponse matchSellAsset(UUID customerId, String assetName, BigDecimal price, BigDecimal size) throws BusinessException {

        Optional<AssetEntity> assetOpt = assetRepository.findByCustomerIdAndAssetNameWithLock(customerId, assetName);

        if (assetOpt.isEmpty() || assetOpt.get().getUsableSize().compareTo(size) < 0) {
            throw new BusinessException(ExceptionConstants.INSUFFICIENT_ASSET, assetName);
        }

        AssetEntity assetEntity = assetOpt.get();

        BigDecimal calculatedSize = TRY_ASSET.equals(assetName) ? price.multiply(size) : size;
        assetEntity.setSize(assetEntity.getSize().subtract(calculatedSize));
        AssetEntity newAssetEntity = assetRepository.save(assetEntity);

        if (!TRY_ASSET.equals(assetName)) {
            matchBuyingAsset(customerId, TRY_ASSET, price, size);
        }
        return AssetMapper.INSTANCE.toResponse(newAssetEntity);
    }
}
