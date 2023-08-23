package io.github.onecx.parameters.rs.external.v2.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gen.io.github.onecx.parameters.rs.v2.model.ApplicationParameterDTOV2;
import io.github.onecx.parameters.domain.models.ApplicationParameterData;

@Mapper
public interface ApplicationParameterDataMapperV2 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleType", constant = "SYSTEM")
    @Mapping(target = "valueRangeFrom", source = "rangeFrom")
    @Mapping(target = "valueRangeTo", source = "rangeTo")
    ApplicationParameterDTOV2 map(ApplicationParameterData entity, @MappingTarget ApplicationParameterDTOV2 dto);
}
