package org.tkit.onecx.parameters.rs.external.v2.mappers;

import java.util.List;

import org.mapstruct.*;
import org.tkit.onecx.parameters.domain.models.ApplicationParameter;

import gen.org.tkit.onecx.parameters.rs.v2.model.ApplicationParameterDTOV2;

@Mapper
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class ApplicationParameterMapperV2 {

    /**
     * The mapping method for find of list entities.
     *
     * @param data the list of entities.
     * @return the corresponding list of DTO objects.
     */
    public abstract List<ApplicationParameterDTOV2> finds(List<ApplicationParameter> data);

    @Mapping(target = "value", source = "setValue")
    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "roleType", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "valueRangeFrom", ignore = true)
    @Mapping(target = "valueRangeTo", ignore = true)
    public abstract ApplicationParameterDTOV2 map(ApplicationParameter data);

}
