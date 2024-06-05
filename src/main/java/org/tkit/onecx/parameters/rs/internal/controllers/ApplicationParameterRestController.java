package org.tkit.onecx.parameters.rs.internal.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.onecx.parameters.domain.models.ApplicationParameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;
import org.tkit.onecx.parameters.rs.internal.mappers.ExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tki.onecx.parameters.rs.internal.model.*;
import gen.org.tkit.onecx.parameters.rs.internal.ParametersApi;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterRestController implements ParametersApi {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Context
    UriInfo uriInfo;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getAllApplications() {
        var apps = applicationParameterDAO.searchAllApplications();
        return Response.ok(applicationParameterInternalMapper.apps(apps)).build();
    }

    @Override
    public Response getAllKeys(String applicationId, String productName) {
        var criteria = applicationParameterInternalMapper.map(productName, applicationId);
        var keys = applicationParameterDAO.searchAllKeys(criteria);
        return Response.ok(applicationParameterInternalMapper.keys(keys)).build();
    }

    @Override
    public Response getAllApplicationParameters(String applicationId, String productName, String key, String name,
            Integer pageNumber,
            Integer pageSize, List<String> type) {

        var criteria = applicationParameterInternalMapper.map(productName, applicationId, key, name, pageNumber, pageSize,
                type);
        var parameters = applicationParameterDAO.searchByCriteria(criteria);
        ApplicationParameterPageResultDTO results = applicationParameterInternalMapper.map(parameters);
        return Response.ok(results).build();
    }

    @Override
    public Response getParameterById(String id) {
        ApplicationParameter param = applicationParameterDAO.findById(id);
        if (param == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(exceptionMapper.exception(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        ApplicationParameterDTO parameterDTO = applicationParameterInternalMapper.map(param);
        return Response.ok(parameterDTO).build();
    }

    @Override
    @Transactional
    public Response updateParameterValue(String id,
            ApplicationParameterUpdateDTO applicationParameterUpdateDTO) {
        ApplicationParameter applicationParameter = applicationParameterDAO.findById(id);
        if (applicationParameter == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(exceptionMapper.exception(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        applicationParameterInternalMapper.update(applicationParameterUpdateDTO, applicationParameter);
        applicationParameterDAO.update(applicationParameter);
        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

    @Override
    @Transactional
    public Response createParameterValue(ApplicationParameterCreateDTO request) {

        ApplicationParameter param = applicationParameterInternalMapper.create(request);
        param = applicationParameterDAO.create(param);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(param.getId()).build())
                .build();
    }

    @Override
    @Transactional
    public Response deleteParameter(String id) {
        ApplicationParameter parameter = applicationParameterDAO.findById(id);
        if (parameter != null) {
            applicationParameterDAO.delete(parameter);
        }
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
