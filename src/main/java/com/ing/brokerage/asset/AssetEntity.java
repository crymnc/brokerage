package com.ing.brokerage.asset;

import com.ing.brokerage.base.entity.AbstractBaseEntity;
import com.ing.brokerage.constant.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "asset")
@SequenceGenerator(name = Constants.DEFAULT_SEQUENCE_GENERATOR, sequenceName = "asset_id_seq")
@Table(indexes = {
    @Index(name = "as_customer_idx", columnList = "customerId"),
    @Index(name = "as_cid_an_uidx", columnList = "customerId, assetName", unique = true)
})
class AssetEntity extends AbstractBaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "size", nullable = false)
    private BigDecimal size;

    @Column(name = "usable_Size", nullable = false)
    private BigDecimal usableSize;
}
