package io.github.onecx.parameters.rs.external.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.parameters.rs.v3.ExternalApi;
import gen.io.github.onecx.parameters.rs.v3.model.ParametersBucketDTOV3;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameterHistory;
import io.github.onecx.parameters.rs.external.v3.mappers.ApplicationParameterHistoryMapper;
import io.github.onecx.parameters.rs.internal.mappers.ExceptionMapper;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV3 implements ExternalApi {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @Inject
    ApplicationParameterHistoryMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getApplicationParameters(String appId) {
        Map<String, String> applicationParameters = applicationParameterDAO.findAllByApplicationId(appId);
        return Response.ok(applicationParameters).build();
    }

    @Override
    public Response bucketRequest(String appId, ParametersBucketDTOV3 dto) {
        if (dto == null || dto.getParameters().isEmpty()) {
            return Response.status(Response.Status.OK).build();
        }
        List<ApplicationParameterHistory> items = new ArrayList<>();
        dto.getParameters().forEach((key, value) -> items
                .add(mapper.mapItem(value, key, dto.getStart(), dto.getEnd(), dto.getInstanceId(), appId,
                        value.getCurrentValue())));
        historyDAO.create(items);
        return Response.status(Response.Status.OK).build();
    }
}
