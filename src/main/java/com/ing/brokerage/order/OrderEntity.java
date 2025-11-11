package com.ing.brokerage.order;

import com.ing.brokerage.base.entity.AbstractBaseAuditEntity;
import com.ing.brokerage.constant.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
@Entity(name = "stock_order")
@SequenceGenerator(name = Constants.DEFAULT_SEQUENCE_GENERATOR, sequenceName = "stock_order_id_seq")
@Table(indexes = {
    @Index(name = "o_customer_idx", columnList = "customerId")
})
class OrderEntity extends AbstractBaseAuditEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "order_side_id", nullable = false)
    @Convert(converter = OrderSideConverter.class)
    private OrderSide orderSide;

    @Column(name = "size", nullable = false)
    private BigDecimal size;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "order_status_id", nullable = false)
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus orderStatus;
}
