package io.github.onecx.parameters.rs.external.v2.mappers;

import java.util.List;

import org.mapstruct.*;

import gen.io.github.onecx.parameters.rs.v2.model.ApplicationParameterDTOV2;
import io.github.onecx.parameters.domain.models.ApplicationParameter;

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
