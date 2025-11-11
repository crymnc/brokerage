package com.ing.brokerage.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderStatus orderStatus) {

        if (orderStatus == null) {
            return null;
        }
        return orderStatus.getId();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Long id) {

        if (id == null) {
            return null;
        }
        return Stream.of(OrderStatus.values()).filter(o -> o.getId().equals(id)).findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
