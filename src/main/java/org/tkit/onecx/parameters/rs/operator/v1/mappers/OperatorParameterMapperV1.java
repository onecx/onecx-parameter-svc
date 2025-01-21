package org.tkit.onecx.parameters.rs.operator.v1.mappers;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.v1.operator.model.ParameterUpdateRequestOperatorDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class OperatorParameterMapperV1 {

    @Inject
    ObjectMapper objectMapper;

    @Mapping(target = "operator", constant = "true")
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "value", source = "request.value", qualifiedByName = "o2s")
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    public abstract Parameter create(ParameterUpdateRequestOperatorDTOV1 request, String productName, String applicationId,
            String name);

    @Named("o2s")
    public String o2s(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ParameterMapper.MapperException("Error reading parameter value", e);
        }
    }
}
