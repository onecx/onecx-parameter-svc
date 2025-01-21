package org.tkit.onecx.parameters.rs.operator.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.operator.v1.mappers.OperatorExceptionMapperV1;
import org.tkit.onecx.parameters.rs.operator.v1.mappers.OperatorParameterMapperV1;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.v1.model.ProblemDetailResponseDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.operator.OperatorParametersApi;
import gen.org.tkit.onecx.parameters.rs.v1.operator.model.ParameterUpdateRequestOperatorDTOV1;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class OperatorParameterRestControllerV1 implements OperatorParametersApi {

    @Inject
    ParameterDAO parameterDAO;

    @Inject
    OperatorParameterMapperV1 mapper;

    @Inject
    OperatorExceptionMapperV1 exceptionMapper;

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @Override
    public Response createOrUpdateParameterValue(String productName, String applicationId, String name,
            ParameterUpdateRequestOperatorDTOV1 requestDTO) {
        var matchedParam = parameterDAO.findByNameApplicationIdAndProductName(name,
                applicationId, productName);
        if (matchedParam == null) {
            Parameter paramToCreate = mapper.create(requestDTO, productName, applicationId, name);
            parameterDAO.create(paramToCreate);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            mapper.update(requestDTO, matchedParam);
            parameterDAO.update(matchedParam);
            return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
        }
    }
}
