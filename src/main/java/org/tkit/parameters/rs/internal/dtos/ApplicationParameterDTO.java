package org.tkit.parameters.rs.internal.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ApplicationParameterDTO extends TraceableDTO {

    private String name;

    private String description;

    private String applicationId;

    private String key;

    private String setValue;

    private String importValue;

    private String type;

    private String unit;

    private Integer rangeFrom;

    private Integer rangeTo;
}
