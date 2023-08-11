package org.tkit.parameters.rs.internal.mappers;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.parameters.domain.models.ApplicationParameterData;
import org.tkit.parameters.rs.internal.dtos.ApplicationParameterCreateDTO;
import org.tkit.parameters.rs.internal.dtos.ApplicationParameterDTO;
import org.tkit.parameters.rs.internal.dtos.ApplicationParameterUpdateDTO;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
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
    ApplicationParameterDTO map(ApplicationParameterData entity, @MappingTarget ApplicationParameterDTO dto);
}
