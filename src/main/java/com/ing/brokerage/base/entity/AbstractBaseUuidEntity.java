package com.ing.brokerage.base.entity;

import com.ing.brokerage.base.generator.UuidV7Generator;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractBaseUuidEntity {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    private UUID id;
}
