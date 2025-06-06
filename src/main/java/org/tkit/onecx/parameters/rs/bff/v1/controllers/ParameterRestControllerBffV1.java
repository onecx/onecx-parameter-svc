package org.tkit.onecx.parameters.rs.bff.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.parameters.domain.services.ParameterService;
import org.tkit.onecx.parameters.rs.bff.v1.mappers.ExceptionMapperBffV1;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.v1.bff.ParametersBffApi;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkRequestBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ProblemDetailResponseBffDTOV1;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerBffV1 implements ParametersBffApi {

    @Inject
    ExceptionMapperBffV1 exceptionMapper;

    @Inject
    ParameterService parameterService;

    @Override
    public Response getParametersByProductsAndAppIds(ParametersBulkRequestBffDTOV1 request) {
        var result = parameterService.getGroupedParametersByProductsAndApps(request.getProducts());
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseBffDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

}
