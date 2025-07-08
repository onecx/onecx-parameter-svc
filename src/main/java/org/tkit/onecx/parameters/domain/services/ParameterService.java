package org.tkit.onecx.parameters.domain.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.bff.v1.mappers.ParameterMapperBffV1;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;

import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryPageResultDTO;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParameterBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkResponseBffDTOV1;

@ApplicationScoped
public class ParameterService {

    @Inject
    ParameterDAO dao;

    @Inject
    HistoryDAO historyDAO;

    @Inject
    ParameterMapper applicationParameterInternalMapper;

    @Inject
    ParameterMapperBffV1 parameterBffMapper;

    @Transactional
    public void importParameters(List<Parameter> create, List<Parameter> update) {
        dao.create(create);
        dao.update(update);
    }

    @Transactional
    public void operatorImportParameters(String productName, String applicationId, List<Parameter> request) {
        var params = dao.findAllByProductNameAndApplicationId(productName, applicationId);
        var map = params.stream().collect(Collectors.toMap(Parameter::getName, p -> p));

        var update = new ArrayList<Parameter>();
        var create = new ArrayList<Parameter>();

        //item == item to import
        //map current available params
        for (var item : request) {
            var parameter = map.get(item.getName());
            if (parameter == null) {
                create.add(item);
            } else {
                map.remove(item.getName());
                update.add(parameter);

                parameter.setOperator(item.isOperator());
                parameter.setDescription(item.getDescription());
                parameter.setDisplayName(item.getDisplayName());
                parameter.setImportValue(item.getImportValue());
            }
        }

        // update all not imported parameters to operator false
        for (var param : map.values()) {
            param.setOperator(false);
            update.add(param);
        }

        // create or update
        dao.create(create);
        dao.update(update);
    }

    @Transactional
    public HistoryPageResultDTO getLatestHistoryEntries(HistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        var pageResult = applicationParameterInternalMapper.mapHistory(parametersHistories);
        pageResult.getStream().forEach(historyDTO -> {
            var parameter = dao.findByNameApplicationIdAndProductName(historyDTO.getName(), historyDTO.getApplicationId(),
                    historyDTO.getProductName());
            if (parameter != null) {
                historyDTO.setParameterId(parameter.getId());
            }
        });
        return pageResult;
    }

    public ParametersBulkResponseBffDTOV1 getGroupedParametersByProductsAndApps(
            Map<String, Set<String>> request) {
        var parameters = dao.findAllByProductNamesAndApplicationIds(request);
        var grouped = parameters.stream()
                .map(parameter -> parameterBffMapper.parameterToParameterBffDTOV1(parameter))
                .collect(Collectors.groupingBy(
                        ParameterBffDTOV1::getProductName,
                        Collectors.groupingBy(ParameterBffDTOV1::getApplicationId)));
        ParametersBulkResponseBffDTOV1 response = new ParametersBulkResponseBffDTOV1();
        response.setProducts(grouped);
        return response;
    }
}
