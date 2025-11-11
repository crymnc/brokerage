package com.ing.brokerage.base.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ing.brokerage.constant.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractBaseAuditUuidEntity extends AbstractBaseUuidEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = Constants.DEFAULT_DATE_FORMAT)
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @CreatedDate
    protected OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, columnDefinition = "varchar(36) default '-1'")
    @CreatedBy
    protected String createdBy;

    @Column(name = "updated_at")
    @JsonFormat(pattern = Constants.DEFAULT_DATE_FORMAT)
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @LastModifiedDate
    protected OffsetDateTime updatedAt;

    @Column(name = "updated_by", columnDefinition = "varchar(36) default '-1'")
    @LastModifiedBy
    protected String updatedBy;

    @Version
    @Column(name = "update_version")
    protected Integer updateVersion;
}
