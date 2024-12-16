package org.tkit.onecx.parameters.domain.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PARAMETER_HISTORY")
@SuppressWarnings("java:S2160")
public class History extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * The application parameter key.
     */
    @Column(name = "KEY")
    private String key;

    /**
     * The application.
     */
    @Column(name = "APPLICATION_ID")
    private String applicationId;

    /**
     * The product
     */
    @Column(name = "PRODUCT_NAME")
    private String productName;

    /**
     * The application parameter type.
     */
    @Column(name = "VALUE_TYPE")
    private String type;

    /**
     * The application parameter used value.
     */
    @Column(name = "USED_VALUE", columnDefinition = "varchar(1000)")
    private String usedValue;

    /**
     * The application parameter used value.
     */
    @Column(name = "DEFAULT_VALUE", columnDefinition = "varchar(1000)")
    private String defaultValue;

    /**
     * Count of hit
     */
    @Column(name = "COUNT")
    private Long count;

    /**
     * Interval start time
     */
    @Column(name = "INTERVAL_START")
    private LocalDateTime start;

    /**
     * Interval end time
     */
    @Column(name = "INTERVAL_END")
    private LocalDateTime end;

    /**
     * The instance ID.
     */
    @Column(name = "INSTANCE_ID")
    private String instanceId;
}
