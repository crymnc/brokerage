package com.ing.brokerage.base.entity;

import com.ing.brokerage.constant.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = Constants.DEFAULT_SEQUENCE_GENERATOR)
    @Column(name = "id", nullable = false, updatable = false)
    protected Long id;
}
