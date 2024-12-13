package org.tkit.onecx.parameters.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.parameters.domain.daos.ParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.models.ParameterHistory;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.internal.HistoriesApi;
import gen.org.tkit.onecx.parameters.rs.internal.model.ParameterHistoryCountCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.ParameterHistoryCriteriaDTO;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class HistoryRestController implements HistoriesApi {

    @Inject
    ParameterMapper applicationParameterInternalMapper;

    @Inject
    ParameterHistoryDAO historyDAO;

    @Override
    public Response getAllParametersHistoryLatest(ParameterHistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getAllParametersHistory(ParameterHistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getParametersHistoryById(String id) {
        ParameterHistory parameter = historyDAO.findById(id);
        if (parameter == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(applicationParameterInternalMapper.mapHistory(parameter)).build();
    }

    @Override
    public Response getCountsByCriteria(ParameterHistoryCountCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var counts = historyDAO.searchCountsByCriteria(criteria);
        var results = applicationParameterInternalMapper.mapCountList(counts);
        return Response.ok(results).build();
    }
}
