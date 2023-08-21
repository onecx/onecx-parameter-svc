package io.github.onecx.parameters.rs.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.github.onecx.parameters.domain.criteria.ApplicationParameterHistorySearchCriteria;
import io.github.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameterHistory;
import io.github.onecx.parameters.rs.internal.dtos.ApplicationParameterHistoryDTO;
import io.github.onecx.parameters.rs.internal.dtos.ApplicationParameterHistoryPageResultDTO;
import io.github.onecx.parameters.rs.internal.dtos.ApplicationParameterHistorySearchCriteriaDTO;
import io.github.onecx.parameters.rs.internal.dtos.ParameterHistoryCountDTO;
import io.github.onecx.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;

@ApplicationScoped
@Tag(name = "internal")
@Path("/histories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterHistoryRestController {

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @GET
    @Path("latest")
    @Operation(operationId = "getAllApplicationParametersHistoryLatest", description = "Find all parameters history latest")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterHistoryPageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllApplicationParametersHistoryLatest(@BeanParam ApplicationParameterHistorySearchCriteriaDTO dto) {
        var criteria = applicationParameterInternalMapper.map(dto);
        var parametersHistories = historyDAO.searchOnlyLatestByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @GET
    @Operation(operationId = "getAllApplicationParametersHistory", description = "Find all parameters history")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterHistoryPageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllApplicationParametersHistory(@BeanParam ApplicationParameterHistorySearchCriteriaDTO dto) {
        var criteria = applicationParameterInternalMapper.map(dto);
        var parametersHistories = historyDAO.searchByCriteria(criteria);
        return Response.ok(applicationParameterInternalMapper.mapHistory(parametersHistories)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getApplicationParametersHistoryById", description = "Find parameters history by Id")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterHistoryDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getApplicationParametersHistoryById(@PathParam("id") String id) {
        ApplicationParameterHistory parameter = historyDAO.findById(id);
        if (parameter == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(applicationParameterInternalMapper.mapHistory(parameter)).build();
    }

    @GET
    @Path("/counts")
    @Operation(operationId = "getCountsByCriteria", description = "Get creation dates and counts by criteria")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ParameterHistoryCountDTO.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getCountsByCriteria(@BeanParam ApplicationParameterHistorySearchCriteriaDTO dto) {
        ApplicationParameterHistorySearchCriteria criteria = applicationParameterInternalMapper.map(dto);
        var counts = historyDAO.searchCountsByCriteria(criteria);
        var results = applicationParameterInternalMapper.mapCountList(counts);
        return Response.ok(results).build();
    }

}
