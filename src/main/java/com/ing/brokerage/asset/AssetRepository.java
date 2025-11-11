package com.ing.brokerage.asset;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface AssetRepository extends JpaRepository<AssetEntity, Long>, JpaSpecificationExecutor<AssetEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM asset a WHERE a.customerId = :customerId AND a.assetName = :assetName")
    Optional<AssetEntity> findByCustomerIdAndAssetNameWithLock(
        @Param("customerId") UUID customerId,
        @Param("assetName") String assetName
    );

    List<AssetEntity> findAllByCustomerId(UUID customerId);
}
