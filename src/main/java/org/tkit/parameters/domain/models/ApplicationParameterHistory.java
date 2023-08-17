package org.tkit.parameters.domain.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "APM_APP_PARAM_HISTORY")
@SuppressWarnings("java:S2160")
public class ApplicationParameterHistory extends TraceableEntity {
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
     * The application.
     */
    @Column(name = "APPLICATION_ID")
    private String applicationId;

    /**
     * The application parameter type.
     */
    @Column(name = "VALUE_TYPE")
    private String type;

    /**
     * The application parameter used value.
     */
    @Column(name = "USED_VALUE")
    private String usedValue;

    /**
     * The application parameter used value.
     */
    @Column(name = "DEFAULT_VALUE")
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
