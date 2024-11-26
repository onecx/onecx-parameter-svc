package org.tkit.onecx.parameters.rs.external.v1.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.onecx.parameters.rs.external.v1.mappers.ApplicationParameterHistoryMapper;
import org.tkit.onecx.parameters.rs.external.v1.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.v1.ParameterApi;
import gen.org.tkit.onecx.parameters.rs.v1.model.ParametersBucketDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.model.ProblemDetailResponseDTOV1;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV1 implements ParameterApi {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @Inject
    ApplicationParameterHistoryMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getApplicationParameters(String productName, String appId) {
        Map<String, String> applicationParameters = applicationParameterDAO.findAllByProductNameAndApplicationId(productName,
                appId);
        return Response.ok(applicationParameters).build();
    }

    @Override
    public Response bucketRequest(String productName, String appId, ParametersBucketDTOV1 dto) {
        if (dto.getParameters() == null || dto.getParameters().isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<ApplicationParameterHistory> items = new ArrayList<>();
        dto.getParameters().forEach((key, value) -> items
                .add(mapper.mapItem(value, key, dto, productName, appId,
                        value.getCurrentValue())));
        historyDAO.create(items);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
