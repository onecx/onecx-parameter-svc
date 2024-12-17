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
@Table(name = "HISTORY")
@SuppressWarnings("java:S2160")
public class History extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID", nullable = false)
    private String tenantId;

    /**
     * The parameter key.
     */
    @Column(name = "NAME", nullable = false)
    private String name;

    /**
     * The application.
     */
    @Column(name = "APP_ID", nullable = false)
    private String applicationId;

    /**
     * The product
     */
    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    /**
     * The parameter type.
     */
    @Column(name = "VALUE_TYPE")
    private String type;

    /**
     * The parameter used value.
     */
    @Column(name = "USED_VALUE", columnDefinition = "varchar(5000)")
    private String usedValue;

    /**
     * The parameter used value.
     */
    @Column(name = "DEFAULT_VALUE", columnDefinition = "varchar(5000)")
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
