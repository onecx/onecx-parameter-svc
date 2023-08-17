package org.tkit.parameters.rs.external.v3.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ParameterInfoDTO {

    private Long count;

    private String type;

    private String defaultValue;

    private String currentValue;

}
