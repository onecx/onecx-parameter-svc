package org.tkit.parameters.rs.internal.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ApplicationParameterUpdateDTO {

    private Object value;

    private String description;

    private String unit;

    private Integer rangeFrom;

    private Integer rangeTo;
}
