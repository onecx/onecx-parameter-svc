package io.github.onecx.parameters.rs.internal.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ApplicationParameterCreateDTO {

    private String key;

    private String applicationId;

    private String value;

    private String type;

    private String description;

    private String unit;

    private Integer rangeFrom;

    private Integer rangeTo;
}
