package com.ing.brokerage.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OrderSideConverter implements AttributeConverter<OrderSide, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderSide orderSide) {

        if (orderSide == null) {
            return null;
        }
        return orderSide.getId();
    }

    @Override
    public OrderSide convertToEntityAttribute(Long id) {

        if (id == null) {
            return null;
        }
        return Stream.of(OrderSide.values()).filter(o -> o.getId().equals(id)).findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
