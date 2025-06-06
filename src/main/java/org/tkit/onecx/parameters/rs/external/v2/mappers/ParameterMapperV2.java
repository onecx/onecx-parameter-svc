package org.tkit.onecx.parameters.rs.external.v2.mappers;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.v2.model.ParameterInfoDTOV2;
import gen.org.tkit.onecx.parameters.rs.v2.model.ParametersBucketDTOV2;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class ParameterMapperV2 {

    @Inject
    ObjectMapper objectMapper;

    public Map<String, Object> mapParameters(Map<String, String> parameters) {
        Map<String, Object> data = new HashMap<>();
        if (parameters == null) {
            return data;
        }
        try {
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) {
                    data.put(e.getKey(), null);
                } else {
                    data.put(e.getKey(), objectMapper.readValue(e.getValue(), Object.class));
                }
            }
        } catch (Exception e) {
            throw new MapperException("Error reading parameter", e);
        }
        return data;
    }

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedValue", source = "dto.currentValue", qualifiedByName = "o2s")
    @Mapping(target = "defaultValue", source = "dto.defaultValue", qualifiedByName = "o2s")
    public abstract History mapItem(ParameterInfoDTOV2 dto, String name, ParametersBucketDTOV2 bucketDTO,
            String productName, String applicationId);

    public static class MapperException extends RuntimeException {

        public MapperException(String msg, Throwable t) {
            super(msg, t);
        }

    }

    @Named("o2s")
    public String o2s(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new MapperException("Error reading parameter value", e);
        }
    }

}
