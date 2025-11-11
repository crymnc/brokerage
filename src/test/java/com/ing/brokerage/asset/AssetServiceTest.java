package com.ing.brokerage.asset;

import static com.ing.brokerage.asset.AssetData.assetEntity;
import static com.ing.brokerage.asset.AssetData.assetSearchRequest;
import static com.ing.brokerage.asset.AssetData.tryAssetEntity;
import static com.ing.brokerage.constant.Constants.TRY_ASSET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ing.brokerage.exception.BusinessException;
import com.ing.brokerage.exception.ExceptionConstants;
import com.ing.brokerage.order.OrderSide;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void shouldListAssets() {

        AssetSearchRequest searchRequest = assetSearchRequest();

        Pageable pageable = PageRequest.of(0, 10);
        Page<AssetEntity> page = new PageImpl<>(List.of(tryAssetEntity(), assetEntity()));

        when(assetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AssetResponse> result = assetService.listAssets(searchRequest, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAssetName()).isEqualTo(TRY_ASSET);
        assertThat(result.getContent().get(1).getAssetName()).isEqualTo("TEST");

        verify(assetRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldFilterByAssetName_WhenListAssets() {

        AssetSearchRequest searchRequest = assetSearchRequest();

        Pageable pageable = PageRequest.of(0, 10);
        Page<AssetEntity> page = new PageImpl<>(List.of(assetEntity()));

        when(assetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AssetResponse> result = assetService.listAssets(searchRequest, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAssetName()).isEqualTo("TEST");
    }

    @Test
    void shouldReturnEmptyPage_IfNoAssetsFound_WhenListAssets() {

        AssetSearchRequest searchRequest = assetSearchRequest();

        Pageable pageable = PageRequest.of(0, 10);
        Page<AssetEntity> emptyPage = new PageImpl<>(List.of());

        when(assetRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<AssetResponse> result = assetService.listAssets(searchRequest, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldLockTryAsset() throws Exception {

        UUID customerId = UUID.randomUUID();
        BigDecimal lockSize = BigDecimal.valueOf(1000);

        AssetEntity tryAsset = tryAssetEntity();
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), any()))
            .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(AssetEntity.class))).thenReturn(tryAsset);

        AssetResponse response = assetService.lockAsset(customerId, "TEST", lockSize);

        assertThat(response).isNotNull();
        assertThat(response.getAssetName()).isEqualTo("TRY");

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository).save(captor.capture());

        AssetEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUsableSize()).isEqualTo(BigDecimal.valueOf(4000));
    }

    @Test
    void shouldThrowException_IfAssetNotFound_WhenLockAsset() {

        BigDecimal lockSize = BigDecimal.valueOf(1000);

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), any()))
            .thenReturn(Optional.empty());

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> assetService.lockAsset(UUID.randomUUID(), "TEST", lockSize));
        Assertions.assertTrue(ex.getMessageKey().contains(ExceptionConstants.INSUFFICIENT_ASSET));
    }

    @Test
    void shouldThrowException_IfInsufficientUsableSize_WhenLockAsset() {

        BigDecimal lockSize = BigDecimal.valueOf(6000);

        AssetEntity tryAsset = tryAssetEntity();
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), any()))
            .thenReturn(Optional.of(tryAsset));

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> assetService.lockAsset(UUID.randomUUID(), TRY_ASSET, lockSize));
        Assertions.assertTrue(ex.getMessageKey().contains(ExceptionConstants.INSUFFICIENT_ASSET));
    }

    @Test
    void shouldUnlockAsset_IfAssetExists_WhenUnlockAsset() throws Exception {

        UUID customerId = UUID.randomUUID();
        BigDecimal unlockSize = BigDecimal.valueOf(1000);

        AssetEntity tryAsset = tryAssetEntity();
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(AssetEntity.class))).thenReturn(tryAsset);

        AssetResponse response = assetService.unlockAsset(customerId, TRY_ASSET, unlockSize);

        assertThat(response).isNotNull();

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository).save(captor.capture());

        AssetEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUsableSize()).isEqualTo(BigDecimal.valueOf(6000));
    }

    @Test
    void shouldThrowException_IfAssetNotFound_WhenUnlockAsset() {

        BigDecimal unlockSize = BigDecimal.valueOf(1000);

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), any()))
            .thenReturn(Optional.empty());

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> assetService.unlockAsset(UUID.randomUUID(), "TEST", unlockSize));
        Assertions.assertEquals(ExceptionConstants.NO_ASSET_TO_UNLOCK, ex.getMessageKey());
    }

    @Test
    void shouldMatchBuyingAsset_IfOrderSideIsBuy_WhenMatchTradingAsset() throws Exception {

        UUID customerId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(tryAssetEntity(), assetEntity());

        AssetResponse response = assetService.matchTradingAsset(customerId, "TEST", price, size, OrderSide.BUY);

        assertThat(response).isNotNull();
        verify(assetRepository, times(2)).save(any(AssetEntity.class));
    }

    @Test
    void shouldMatchSellAsset_IfOrderSideIsSell_WhenMatchTradingAsset() throws Exception {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(assetEntity(), tryAssetEntity());

        AssetResponse response = assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size, OrderSide.SELL);

        assertThat(response).isNotNull();
        verify(assetRepository, times(2)).save(any(AssetEntity.class));
    }

    @Test
    void shouldIncreaseAssetSize_WhenMatchBuyingAsset() throws Exception {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(tryAssetEntity(), assetEntity());

        assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size, OrderSide.BUY);

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository, times(2)).save(captor.capture());

        List<AssetEntity> savedEntities = captor.getAllValues();
        AssetEntity testEntity = savedEntities.stream()
                                              .filter(e -> "TEST".equals(e.getAssetName()))
                                              .findFirst()
                                              .orElseThrow();

        assertThat(testEntity.getSize()).isEqualTo(BigDecimal.valueOf(110));
        assertThat(testEntity.getUsableSize()).isEqualTo(BigDecimal.valueOf(90));
    }

    @Test
    void shouldCreateNewAsset_IfAssetNotExists_WhenMatchBuyingAsset() throws Exception {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.empty());

        AssetEntity newAsset = new AssetEntity();
        newAsset.setId(3L);
        newAsset.setCustomerId(UUID.randomUUID());
        newAsset.setAssetName("TEST");
        newAsset.setSize(size);
        newAsset.setUsableSize(size);

        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(tryAssetEntity(), newAsset);

        assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size, OrderSide.BUY);

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository, times(2)).save(captor.capture());

        List<AssetEntity> savedEntities = captor.getAllValues();
        AssetEntity testEntity = savedEntities.stream()
                                              .filter(e -> "TEST".equals(e.getAssetName()))
                                              .findFirst()
                                              .orElseThrow();


        assertThat(testEntity.getAssetName()).isEqualTo("TEST");
        assertThat(testEntity.getSize()).isEqualTo(size);
        assertThat(testEntity.getUsableSize()).isEqualTo(size);
    }

    @Test
    void shouldDecreaseAssetSize_IfAssetExists_WhenMatchSellAsset() throws Exception {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(assetEntity(), tryAssetEntity());

        assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size, OrderSide.SELL);

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository, times(2)).save(captor.capture());

        List<AssetEntity> savedEntities = captor.getAllValues();
        AssetEntity aaplEntity = savedEntities.stream()
                                              .filter(e -> "TEST".equals(e.getAssetName()))
                                              .findFirst()
                                              .orElseThrow();

        assertThat(aaplEntity.getSize()).isEqualTo(BigDecimal.valueOf(90));
    }

    @Test
    void shouldThrowException_IfInsufficientAsset_WhenMatchSellAsset() {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.valueOf(100);

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size,
                                                                                            OrderSide.SELL));
        Assertions.assertTrue(ex.getMessageKey().contains(ExceptionConstants.INSUFFICIENT_ASSET));
    }

    @Test
    void shouldThrowException_IfAssetNotFound_WhenMatchSellAsset() {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.empty());

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                                                       () -> assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size,
                                                                                            OrderSide.SELL));
        Assertions.assertTrue(ex.getMessageKey().contains(ExceptionConstants.INSUFFICIENT_ASSET));
    }

    @Test
    void shouldHandleTRYAsset_WithPriceMultiplication_WhenMatchBuyingAsset() throws Exception {

        BigDecimal price = BigDecimal.valueOf(2);
        BigDecimal size = BigDecimal.valueOf(100);

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(tryAssetEntity());

        assetService.matchTradingAsset(UUID.randomUUID(), TRY_ASSET, price, size, OrderSide.BUY);

        ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);
        verify(assetRepository).save(captor.capture());

        AssetEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getSize()).isEqualTo(BigDecimal.valueOf(10200));
        assertThat(savedEntity.getUsableSize()).isEqualTo(BigDecimal.valueOf(5200));
    }

    @Test
    void shouldUsePessimisticLock_WhenConcurrentAccess() throws Exception {

        BigDecimal price = BigDecimal.valueOf(150);
        BigDecimal size = BigDecimal.TEN;

        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET)))
            .thenReturn(Optional.of(tryAssetEntity()));
        when(assetRepository.findByCustomerIdAndAssetNameWithLock(any(), eq("TEST")))
            .thenReturn(Optional.of(assetEntity()));
        when(assetRepository.save(any(AssetEntity.class)))
            .thenReturn(tryAssetEntity(), assetEntity());

        assetService.matchTradingAsset(UUID.randomUUID(), "TEST", price, size, OrderSide.BUY);

        verify(assetRepository).findByCustomerIdAndAssetNameWithLock(any(), eq(TRY_ASSET));
        verify(assetRepository).findByCustomerIdAndAssetNameWithLock(any(), eq("TEST"));
    }
}
