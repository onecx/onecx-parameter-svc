package org.tkit.parameters.rs.external.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.tkit.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.parameters.rs.external.v3.mappers.ApplicationParameterHistoryMapper;
import org.tkit.parameters.rs.external.v3.models.ParametersBucketDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Path("/v3")
@Tag(name = "external")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV3 {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterHistoryDAO historyDAO;

    @Inject
    ApplicationParameterHistoryMapper mapper;

    @GET
    @Path("/{appId}/parameters")
    @Operation(operationId = "getApplicationParameters", description = "Get parameters by application id")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Object.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getApplicationParameters(@PathParam(value = "appId") String appId) {
        Map<String, String> applicationParameters = applicationParameterDAO.findAllByApplicationId(appId);
        return Response.ok(applicationParameters).build();
    }

    @POST
    @Path("/{appId}/history")
    @Operation(operationId = "createOrUpdateParameters", description = "Update or create parameters for specific application")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ParametersBucketDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response bucketRequest(@PathParam(value = "appId") String appId, ParametersBucketDTO dto) {
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
