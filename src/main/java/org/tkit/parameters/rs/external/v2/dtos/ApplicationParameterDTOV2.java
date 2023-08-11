package org.tkit.parameters.rs.external.v2.dtos;

import org.tkit.parameters.rs.external.v2.dtos.enums.ApplicationParameterRoleTypeDTOV2;
import org.tkit.parameters.rs.external.v2.dtos.enums.ApplicationParameterTypeDTOV2;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ApplicationParameterDTOV2 extends AbstractEntityDTOV2 {

    private String key;
    private String name;
    private String value;
    private String description;
    private ApplicationParameterTypeDTOV2 type;
    private String applicationId;
    private ApplicationParameterRoleTypeDTOV2 roleType;
    private String unit;
    private Integer valueRangeFrom;
    private Integer valueRangeTo;
}
