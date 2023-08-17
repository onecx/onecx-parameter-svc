package org.tkit.parameters.rs.internal.mappers;

import java.util.List;

import org.mapstruct.*;
import org.tkit.parameters.domain.criteria.ApplicationParameterHistorySearchCriteria;
import org.tkit.parameters.domain.criteria.ApplicationParameterSearchCriteria;
import org.tkit.parameters.domain.criteria.KeysSearchCriteria;
import org.tkit.parameters.domain.models.ApplicationParameter;
import org.tkit.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.parameters.domain.models.ParameterHistoryCountTuple;
import org.tkit.parameters.rs.internal.dtos.*;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { OffsetDateTimeMapper.class })
public interface ApplicationParameterInternalMapper {

    KeysSearchCriteria map(KeysSearchCriteriaDTO dto);

    KeysPageResultDTO keys(PageResult<String> page);

    ApplicationsPageResultDTO apps(PageResult<String> page);

    ApplicationParameterHistorySearchCriteria map(ApplicationParameterHistorySearchCriteriaDTO dto);

    ApplicationParameterSearchCriteria map(ApplicationParameterSearchCriteriaDTO dto);

    ApplicationParameterHistoryPageResultDTO mapHistory(PageResult<ApplicationParameterHistory> page);

    ApplicationParameterHistoryDTO mapHistory(ApplicationParameterHistory applicationParameterHistory);

    List<ParameterHistoryCountDTO> mapCountList(List<ParameterHistoryCountTuple> count);

    ApplicationParameterPageResultDTO map(PageResult<ApplicationParameter> page);

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

    RestExceptionDTO createRestException(String errorCode, String message);
}
