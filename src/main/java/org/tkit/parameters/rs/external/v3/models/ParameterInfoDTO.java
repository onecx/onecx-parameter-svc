package org.tkit.parameters.rs.external.v3.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ParameterInfoDTO {

    public Long count;

    public String type;

    public String defaultValue;

    public String currentValue;

}
