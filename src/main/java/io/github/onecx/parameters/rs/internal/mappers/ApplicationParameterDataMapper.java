package io.github.onecx.parameters.rs.internal.mappers;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterCreateDTO;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterDTO;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterUpdateDTO;
import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.github.onecx.parameters.domain.models.ApplicationParameterData;

@Mapper
public interface ApplicationParameterDataMapper {

    default ApplicationParameterData update(ApplicationParameterUpdateDTO dto, ApplicationParameterData entity) {
        if (StringUtils.isNotBlank(dto.getUnit())) {
            entity.setUnit(dto.getUnit());
        }
        if (dto.getRangeFrom() != null) {
            entity.setRangeFrom(dto.getRangeFrom());
        }
        if (dto.getRangeTo() != null) {
            entity.setRangeTo(dto.getRangeTo());
        }
        return entity;
    }

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "setValue", source = "value")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "importValue", ignore = true)
    ApplicationParameter create(ApplicationParameterCreateDTO dto);

    default ApplicationParameterData create(ApplicationParameterUpdateDTO dto, String applicationParameterId) {
        ApplicationParameterData entity = new ApplicationParameterData();
        entity.setApplicationParameterGuid(applicationParameterId);
        entity.setUnit(dto.getUnit());
        entity.setRangeFrom(dto.getRangeFrom());
        entity.setRangeTo(dto.getRangeTo());
        return entity;
    }

    default ApplicationParameterData create(ApplicationParameterCreateDTO dto, String applicationParameterId) {
        ApplicationParameterData entity = new ApplicationParameterData();
        entity.setApplicationParameterGuid(applicationParameterId);
        entity.setUnit(dto.getUnit());
        entity.setRangeFrom(dto.getRangeFrom());
        entity.setRangeTo(dto.getRangeTo());
        return entity;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "setValue", ignore = true)
    @Mapping(target = "importValue", ignore = true)
    @Mapping(target = "type", ignore = true)
    ApplicationParameterDTO map(ApplicationParameterData entity, @MappingTarget ApplicationParameterDTO dto);
}
