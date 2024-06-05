package org.tkit.onecx.parameters.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "APM_APP_PARAM", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "APPLICATION_ID", "PRODUCT_NAME", "PARAM_KEY", "TENANT_ID" }) })
@SuppressWarnings("java:S2160")
public class ApplicationParameter extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * The UID for this class.
     */
    private static final long serialVersionUID = 9009055375282015896L;

    /**
     * The application parameter key.
     */
    @Column(name = "PARAM_KEY")
    private String key;

    /**
     * The application parameter name.
     */
    @Column(name = "PARAM_NAME")
    private String name;

    /**
     * The name of the parameter as it appears in functional specifications
     * (FSS).
     */
    @Column(name = "PARAM_DESCRIPTION")
    private String description;

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
     * The application parameter set value.
     */
    @Column(name = "SET_VALUE")
    private String setValue;

    /**
     * The application parameter set value.
     */
    @Column(name = "IMPORT_VALUE")
    private String importValue;
}
