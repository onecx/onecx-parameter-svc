package org.tkit.onecx.parameters.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.internal.HistoriesApi;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCountCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.model.HistoryCriteriaDTO;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class HistoryRestController implements HistoriesApi {

    @Inject
    ParameterMapper applicationParameterInternalMapper;

    @Inject
    HistoryDAO historyDAO;

    @Override
    public Response getAllParametersHistoryLatest(HistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getAllHistoryProducts() {
        var apps = historyDAO.searchAllProductNamesAndApplicationIds();
        return Response.ok(applicationParameterInternalMapper.apps(apps)).build();
    }

    @Override
    public Response getAllParametersHistory(HistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getParametersHistoryById(String id) {
        History parameter = historyDAO.findById(id);
        if (parameter == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(applicationParameterInternalMapper.mapHistory(parameter)).build();
    }

    @Override
    public Response getCountsByCriteria(HistoryCountCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var counts = historyDAO.searchCountsByCriteria(criteria);
        var results = applicationParameterInternalMapper.mapCountList(counts);
        return Response.ok(results).build();
    }
}
