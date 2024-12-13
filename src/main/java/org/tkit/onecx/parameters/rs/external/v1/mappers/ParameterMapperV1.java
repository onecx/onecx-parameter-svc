package org.tkit.onecx.parameters.rs.external.v1.mappers;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.parameters.domain.models.ParameterHistory;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.v1.model.ParameterInfoDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.model.ParametersBucketDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class ParameterMapperV1 {

    @Inject
    ObjectMapper objectMapper;

    public Map<String, Object> mapParameters(Map<String, String> parameters) {
        Map<String, Object> data = new HashMap<>();
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
    public abstract ParameterHistory mapItem(ParameterInfoDTOV1 dto, String key, ParametersBucketDTOV1 bucketDTO,
            String productName,
            String applicationId, String usedValue);

    public static class MapperException extends RuntimeException {

        public MapperException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
