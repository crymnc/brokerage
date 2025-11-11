package com.ing.brokerage.customer;

import com.ing.brokerage.base.entity.AbstractBaseAuditUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "customer")
@Table(indexes = {
    @Index(name = "c_username_uidx", columnList = "username", unique = true)
})
public class CustomerEntity extends AbstractBaseAuditUuidEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role_id", nullable = false)
    @Convert(converter = RoleConverter.class)
    private Role role;

}
