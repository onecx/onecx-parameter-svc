package org.tkit.onecx.parameters.rs.internal.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.*;
import org.tkit.onecx.parameters.domain.criteria.ApplicationParameterHistorySearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.ApplicationParameterSearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.KeysSearchCriteria;
import org.tkit.onecx.parameters.domain.models.ApplicationParameter;
import org.tkit.onecx.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.onecx.parameters.domain.models.ApplicationTuple;
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

    default List<ProductDTO> apps(List<ApplicationTuple> applicationTuple) {
        Map<String, List<String>> productMap = new HashMap<>();

        for (ApplicationTuple singleApplicationTuple : applicationTuple) {
            productMap
                    .computeIfAbsent(singleApplicationTuple.productName(), k -> new ArrayList<>())
                    .add(singleApplicationTuple.appId());
        }

        List<ProductDTO> products = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : productMap.entrySet()) {
            products.add(new ProductDTO().productName(entry.getKey()).applications(entry.getValue()));
        }

        return products;
    }

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterHistorySearchCriteria map(ApplicationParameterHistoryCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterHistorySearchCriteria map(ParameterHistoryCountCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    ApplicationParameterSearchCriteria map(ParameterSearchCriteriaDTO criteriaDTO);

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
