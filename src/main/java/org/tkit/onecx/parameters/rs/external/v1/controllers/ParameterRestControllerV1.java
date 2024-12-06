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
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.daos.ParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.models.ParameterHistory;
import org.tkit.onecx.parameters.rs.external.v1.mappers.ExceptionMapperV1;
import org.tkit.onecx.parameters.rs.external.v1.mappers.ParameterMapperV1;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.v1.ParameterApi;
import gen.org.tkit.onecx.parameters.rs.v1.model.ParametersBucketDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.model.ProblemDetailResponseDTOV1;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV1 implements ParameterApi {

    @Inject
    ParameterDAO applicationParameterDAO;

    @Inject
    ParameterHistoryDAO historyDAO;

    @Inject
    ParameterMapperV1 mapper;

    @Inject
    ExceptionMapperV1 exceptionMapper;

    @Override
    public Response getParameters(String productName, String appId) {
        Map<String, String> applicationParameters = applicationParameterDAO.findAllByProductNameAndApplicationId(productName,
                appId);
        if (applicationParameters.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.mapParameters(applicationParameters)).build();
    }

    @Override
    public Response bucketRequest(String productName, String appId, ParametersBucketDTOV1 dto) {
        if (dto.getParameters() == null || dto.getParameters().isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<ParameterHistory> items = new ArrayList<>();
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
