package io.github.onecx.parameters.domain.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "APM_APP_PARAM_DATA")
public class ApplicationParameterData {

    @Id
    @Column(name = "GUID")
    private String id = UUID.randomUUID().toString();

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "RANGE_FROM")
    private Integer rangeFrom;

    @Column(name = "RANGE_TO")
    private Integer rangeTo;

    @Column(name = "PARAMETER_GUID", nullable = false, unique = true)
    private String applicationParameterGuid;
}
