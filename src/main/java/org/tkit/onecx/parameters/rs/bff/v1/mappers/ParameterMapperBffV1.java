package org.tkit.onecx.parameters.rs.bff.v1.mappers;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParameterBffDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class ParameterMapperBffV1 {
    @Inject
    ObjectMapper objectMapper;

    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "applicationId", target = "applicationId")
    @Mapping(target = "value", source = "value", qualifiedByName = "s2o")
    @Mapping(target = "importValue", source = "importValue", qualifiedByName = "s2o")
    public abstract ParameterBffDTOV1 parameterToParameterBffDTOV1(Parameter parameter);

    @Named("s2o")
    public Object s2o(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new ParameterMapper.MapperException("Error reading parameter value", e);
        }
    }
}
