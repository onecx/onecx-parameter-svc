package org.tkit.onecx.parameters.rs.internal.mappers;

import java.util.List;

import org.mapstruct.*;
import org.tkit.onecx.parameters.domain.criteria.ApplicationParameterHistorySearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.ApplicationParameterSearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.KeysSearchCriteria;
import org.tkit.onecx.parameters.domain.models.ApplicationParameter;
import org.tkit.onecx.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.onecx.parameters.domain.models.ParameterHistoryCountTuple;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tki.onecx.parameters.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ApplicationParameterInternalMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    KeysSearchCriteria map(String productName, String applicationId);

    @Mapping(target = "removeStreamItem", ignore = true)
    KeysPageResultDTO keys(PageResult<String> page);

    @Mapping(target = "removeStreamItem", ignore = true)
    ApplicationsPageResultDTO apps(PageResult<String> page);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterHistorySearchCriteria map(ApplicationParameterHistoryCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterHistorySearchCriteria map(ParameterHistoryCountCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterSearchCriteria map(String productName, String applicationId, String key, String name,
            Integer pageNumber, Integer pageSize,
            List<String> type);

    @Mapping(target = "removeStreamItem", ignore = true)
    ApplicationParameterHistoryPageResultDTO mapHistory(PageResult<ApplicationParameterHistory> page);

    ApplicationParameterHistoryDTO mapHistory(ApplicationParameterHistory applicationParameterHistory);

    List<ParameterHistoryCountDTO> mapCountList(List<ParameterHistoryCountTuple> count);

    @Mapping(target = "removeStreamItem", ignore = true)
    ApplicationParameterPageResultDTO map(PageResult<ApplicationParameter> page);

    @Mapping(target = "value", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "rangeFrom", ignore = true)
    @Mapping(target = "rangeTo", ignore = true)
    ApplicationParameterDTO map(ApplicationParameter applicationParameter);

    @Named("setValue")
    default String objectToSetValue(Object value) {
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    default void update(ApplicationParameterUpdateDTO dto, ApplicationParameter applicationParameter) {
        if (dto == null) {
            return;
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            applicationParameter.setDescription(dto.getDescription());
        }
        if (dto.getValue() != null) {
            applicationParameter.setSetValue(objectToSetValue(dto.getValue()));
        }
    }

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "setValue", source = "value")
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "importValue", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    ApplicationParameter create(ApplicationParameterCreateDTO request);
}