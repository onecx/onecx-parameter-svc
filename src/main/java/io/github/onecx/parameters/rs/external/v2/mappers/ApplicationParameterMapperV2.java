package io.github.onecx.parameters.rs.external.v2.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.github.onecx.parameters.rs.external.v2.dtos.ApplicationParameterDTOV2;

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
    public abstract ApplicationParameterDTOV2 map(ApplicationParameter data);

}
