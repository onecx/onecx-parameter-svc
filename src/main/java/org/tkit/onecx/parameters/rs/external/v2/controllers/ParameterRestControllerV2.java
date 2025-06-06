package org.tkit.onecx.parameters.rs.external.v2.controllers;

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
import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.onecx.parameters.rs.external.v2.mappers.ExceptionMapperV2;
import org.tkit.onecx.parameters.rs.external.v2.mappers.ParameterMapperV2;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.v2.ParameterApiV2;
import gen.org.tkit.onecx.parameters.rs.v2.model.ParametersBucketDTOV2;
import gen.org.tkit.onecx.parameters.rs.v2.model.ProblemDetailResponseDTOV2;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV2 implements ParameterApiV2 {

    @Inject
    ParameterDAO applicationParameterDAO;

    @Inject
    HistoryDAO historyDAO;

    @Inject
    ParameterMapperV2 mapper;

    @Inject
    ExceptionMapperV2 exceptionMapper;

    @Override
    public Response getParameters(String productName, String appId) {
        Map<String, String> applicationParameters = applicationParameterDAO.findAllValuesByProductNameAndApplicationId(
                productName,
                appId);
        if (applicationParameters.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.mapParameters(applicationParameters)).build();
    }

    @Override
    public Response bucketRequest(String productName, String appId, ParametersBucketDTOV2 dto) {
        if (dto.getParameters() == null || dto.getParameters().isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        List<History> items = new ArrayList<>();
        dto.getParameters().forEach((name, value) -> items
                .add(mapper.mapItem(value, name, dto, productName, appId)));
        historyDAO.create(items);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV2> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
