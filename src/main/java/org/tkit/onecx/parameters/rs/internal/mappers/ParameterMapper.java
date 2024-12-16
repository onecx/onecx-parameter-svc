package org.tkit.onecx.parameters.rs.internal.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.mapstruct.*;
import org.tkit.onecx.parameters.domain.criteria.HistorySearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.KeysSearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.ParameterSearchCriteria;
import org.tkit.onecx.parameters.domain.models.ApplicationTuple;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.onecx.parameters.domain.models.HistoryCountTuple;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class ParameterMapper {

    @Inject
    ObjectMapper objectMapper;

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract KeysSearchCriteria map(String productName, String applicationId);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract KeysPageResultDTO keys(PageResult<String> page);

    public List<ProductDTO> apps(List<ApplicationTuple> applicationTuple) {
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
    public abstract HistorySearchCriteria map(HistoryCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract HistorySearchCriteria map(HistoryCountCriteriaDTO criteriaDTO);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract ParameterSearchCriteria map(ParameterSearchCriteriaDTO criteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract HistoryPageResultDTO mapHistory(PageResult<History> page);

    public abstract HistoryDTO mapHistory(History parameterHistory);

    public abstract List<HistoryCountDTO> mapCountList(List<HistoryCountTuple> count);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract ParameterPageResultDTO map(PageResult<Parameter> page);

    @Mapping(target = "value", source = "value", qualifiedByName = "s2o")
    @Mapping(target = "importValue", source = "importValue", qualifiedByName = "s2o")
    public abstract ParameterDTO map(Parameter parameter);

    @Named("s2o")
    public Object s2o(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new MapperException("Error reading parameter value", e);
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

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "value", source = "value", qualifiedByName = "o2s")
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "importValue", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "productName", ignore = true)
    public abstract void update(ParameterUpdateDTO dto, @MappingTarget Parameter parameter);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "value", source = "value", qualifiedByName = "o2s")
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "importValue", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    public abstract Parameter create(ParameterCreateDTO request);

    public static class MapperException extends RuntimeException {

        public MapperException(String msg, Throwable t) {
            super(msg, t);
        }

    }
}
