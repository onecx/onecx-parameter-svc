package io.github.onecx.parameters.rs.external.v3.mappers;

import java.time.OffsetDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.parameters.rs.v3.model.ParameterInfoDTOV3;
import io.github.onecx.parameters.domain.models.ApplicationParameterHistory;

@Mapper(uses = OffsetDateTimeMapper.class)
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApplicationParameterHistoryMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    ApplicationParameterHistory mapItem(ParameterInfoDTOV3 dto, String key, OffsetDateTime start,
            OffsetDateTime end, String instanceId, String applicationId, String usedValue);
}
