package org.tkit.onecx.parameters.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameters.rs.internal.ParametersApi;
import gen.org.tkit.onecx.parameters.rs.internal.model.*;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestController implements ParametersApi {

    @Inject
    ParameterDAO parameterDAO;

    @Inject
    ParameterMapper parameterMapper;

    @Context
    UriInfo uriInfo;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getAllApplications() {
        var apps = parameterDAO.searchAllProductNamesAndApplicationIds();
        return Response.ok(parameterMapper.apps(apps)).build();
    }

    @Override
    public Response getAllNames(String productName, String applicationId) {
        var criteria = parameterMapper.map(productName, applicationId);
        var keys = parameterDAO.searchAllNames(criteria);
        return Response.ok(parameterMapper.names(keys)).build();
    }

    @Override
    public Response searchParametersByCriteria(ParameterSearchCriteriaDTO criteriaDTO) {

        var criteria = parameterMapper.map(criteriaDTO);
        var parameters = parameterDAO.searchByCriteria(criteria);
        ParameterPageResultDTO results = parameterMapper.map(parameters);
        return Response.ok(results).build();
    }

    @Override
    public Response getParameterById(String id) {
        Parameter param = parameterDAO.findById(id);
        if (param == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(exceptionMapper.exception(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        ParameterDTO parameterDTO = parameterMapper.map(param);
        return Response.ok(parameterDTO).build();
    }

    @Override
    public Response updateParameterValue(String id,
            ParameterUpdateDTO parameterUpdateDTO) {
        Parameter parameter = parameterDAO.findById(id);
        if (parameter == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(exceptionMapper.exception(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        parameterMapper.update(parameterUpdateDTO, parameter);
        parameterDAO.update(parameter);
        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

    @Override
    public Response createParameter(ParameterCreateDTO request) {

        Parameter param = parameterMapper.create(request);
        param = parameterDAO.create(param);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(param.getId()).build())
                .build();
    }

    @Override
    public Response deleteParameter(String id) {
        parameterDAO.deleteQueryById(id);
        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
