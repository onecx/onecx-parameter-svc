package org.tkit.onecx.parameters.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.onecx.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.parameters.rs.v1.model.ParameterInfoDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.model.ParametersBucketDTOV1;

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
    ApplicationParameterHistory mapItem(ParameterInfoDTOV1 dto, String key, ParametersBucketDTOV1 bucketDTO, String productName,
            String applicationId, String usedValue);

}
