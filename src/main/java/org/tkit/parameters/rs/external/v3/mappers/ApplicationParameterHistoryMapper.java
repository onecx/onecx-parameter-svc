package org.tkit.parameters.rs.external.v3.mappers;

import java.time.OffsetDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.tkit.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.parameters.rs.external.v3.models.ParameterInfoDTO;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = OffsetDateTimeMapper.class)
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApplicationParameterHistoryMapper {

    ApplicationParameterHistory mapItem(ParameterInfoDTO dto, String key, OffsetDateTime start,
            OffsetDateTime end, String instanceId, String applicationId, String usedValue);
}
