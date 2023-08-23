package io.github.onecx.parameters.rs.internal;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.parameters.rs.internal.HistoriesApi;
import io.github.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameterHistory;
import io.github.onecx.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterHistoryRestController implements HistoriesApi {

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @Override
    public Response getAllApplicationParametersHistoryLatest(String applicationId, String key, Integer pageNumber,
            Integer pageSize, List<String> type) {
        var criteria = applicationParameterInternalMapper.map(applicationId, key, pageNumber, pageSize, type);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @Override
    public Response getAllApplicationParametersHistory(String applicationId, String key, Integer pageNumber, Integer pageSize,
            List<String> type) {
        var criteria = applicationParameterInternalMapper.map(applicationId, key, pageNumber, pageSize, type);
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
    public Response getCountsByCriteria(String applicationId, String key, Integer pageNumber, Integer pageSize,
            List<String> type) {
        var criteria = applicationParameterInternalMapper.map(applicationId, key, pageNumber, pageSize, type);
        var counts = historyDAO.searchCountsByCriteria(criteria);
        var results = applicationParameterInternalMapper.mapCountList(counts);
        return Response.ok(results).build();
    }

}
