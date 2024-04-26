package org.tkit.onecx.parameters.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.onecx.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tki.onecx.parameters.rs.internal.model.ApplicationParameterHistoryCriteriaDTO;
import gen.org.tki.onecx.parameters.rs.internal.model.ParameterHistoryCountCriteriaDTO;
import gen.org.tkit.onecx.parameters.rs.internal.HistoriesApi;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterHistoryRestController implements HistoriesApi {

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @Override
    public Response getAllApplicationParametersHistoryLatest(ApplicationParameterHistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getAllApplicationParametersHistory(ApplicationParameterHistoryCriteriaDTO criteriaDTO) {
        var criteria = applicationParameterInternalMapper.map(criteriaDTO);
        var parametersHistories = historyDAO.searchByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getApplicationParametersHistoryById(String id) {
        ApplicationParameterHistory parameter = historyDAO.findById(id);
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
