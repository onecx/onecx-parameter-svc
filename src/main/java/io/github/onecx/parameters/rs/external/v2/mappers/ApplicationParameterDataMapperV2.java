package io.github.onecx.parameters.rs.external.v2.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import io.github.onecx.parameters.domain.models.ApplicationParameterData;
import io.github.onecx.parameters.rs.external.v2.dtos.ApplicationParameterDTOV2;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ApplicationParameterDataMapperV2 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleType", constant = "SYSTEM")
    @Mapping(target = "valueRangeFrom", source = "rangeFrom")
    @Mapping(target = "valueRangeTo", source = "rangeTo")
    ApplicationParameterDTOV2 map(ApplicationParameterData entity, @MappingTarget ApplicationParameterDTOV2 dto);
}
