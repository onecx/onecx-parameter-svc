package org.tkit.onecx.parameters.domain.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;

import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryPageResultDTO;

@ApplicationScoped
public class ParameterService {

    @Inject
    ParameterDAO dao;

    @Inject
    HistoryDAO historyDAO;

    @Inject
    ParameterMapper applicationParameterInternalMapper;

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
                parameter.setImportValue(item.getValue());
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
}
