package com.ing.brokerage.customer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, Long> {

    @Override
    public Long convertToDatabaseColumn(Role role) {

        if (role == null) {
            return null;
        }
        return role.getId();
    }

    @Override
    public Role convertToEntityAttribute(Long id) {

        if (id == null) {
            return null;
        }
        return Stream.of(Role.values()).filter(o -> o.getId().equals(id)).findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
