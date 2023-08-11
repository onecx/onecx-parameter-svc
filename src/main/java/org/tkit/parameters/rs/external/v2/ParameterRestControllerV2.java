package org.tkit.parameters.rs.external.v2;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.tkit.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.parameters.domain.daos.ApplicationParameterDataDAO;
import org.tkit.parameters.domain.models.ApplicationParameter;
import org.tkit.parameters.domain.models.ApplicationParameterData;
import org.tkit.parameters.rs.external.v2.dtos.AbstractEntityDTOV2;
import org.tkit.parameters.rs.external.v2.dtos.ApplicationParameterDTOV2;
import org.tkit.parameters.rs.external.v2.dtos.enums.ApplicationParameterTypeDTOV2;
import org.tkit.parameters.rs.external.v2.mappers.ApplicationParameterDataMapperV2;
import org.tkit.parameters.rs.external.v2.mappers.ApplicationParameterMapperV2;

@ApplicationScoped
@Path("/v2")
@Tag(name = "external")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV2 {

    @Inject
    ApplicationParameterDAO applicationParameterService;

    @Inject
    ApplicationParameterDataDAO applicationParameterDataDAO;

    @Inject
    ApplicationParameterMapperV2 extApplicationParameterMapperV2;

    @Inject
    ApplicationParameterDataMapperV2 applicationParameterDataMapperV2;

    @GET
    @Path("/parameters")
    @Operation(operationId = "getParameter", description = "Get application parameter by application id and parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterDTOV2.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getParameter(@QueryParam("applicationId") String applicationId,
            @QueryParam("parameterKey") String parameterKey) {
        if (applicationId == null || applicationId.isEmpty() || parameterKey == null || parameterKey.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ApplicationId and parameterKey are mandatory.").build();
        }

        var applicationParameterList = applicationParameterService
                .findByApplicationIdAndParameterKeys(applicationId, List.of(parameterKey));
        return getAllParameters(applicationParameterList);
    }

    @POST
    @Path("/parameters")
    @Operation(operationId = "getAllParameters", description = "Get all application parameters by application id and/or parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterDTOV2.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllParameters(@QueryParam("applicationId") String applicationId, List<String> parametersKeys) {
        if (applicationId == null || applicationId.isEmpty() || parametersKeys == null || parametersKeys.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ApplicationId and parametersKeys are mandatory.")
                    .build();
        }

        var applicationParameterList = applicationParameterService
                .findByApplicationIdAndParameterKeys(applicationId, parametersKeys);
        return getAllParameters(applicationParameterList);
    }

    Response getAllParameters(List<ApplicationParameter> applicationParameterList) {
        if (!applicationParameterList.isEmpty()) {
            List<ApplicationParameterDTOV2> results = extApplicationParameterMapperV2.finds(applicationParameterList);

            // map ApplicationParameterData to ApplicationParameter due to backward compatibility

            var parametersData = applicationParameterDataDAO
                    .findByParameterIds(results.stream().map(AbstractEntityDTOV2::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(ApplicationParameterData::getApplicationParameterGuid, d -> d));

            results.forEach(p -> applicationParameterDataMapperV2.map(parametersData.get(p.getId()), p));

            return Response.ok(results).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/stringParameters")
    @Operation(operationId = "getString", description = "Get application parameter as String by application id and parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getString(@QueryParam("applicationId") String applicationId,
            @QueryParam("parameterKey") String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.STRING, s -> s);
    }

    @GET
    @Path("/longParameters")
    @Operation(operationId = "getLong", description = "Get application parameter as Long by application id and parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Long.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getLong(@QueryParam("applicationId") String applicationId,
            @QueryParam("parameterKey") String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.NUMBER, Long::valueOf);
    }

    @GET
    @Path("/integerParameters")
    @Operation(operationId = "getInteger", description = "Get application parameter as Integer by application id and parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Integer.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getInteger(@QueryParam("applicationId") String applicationId,
            @QueryParam("parameterKey") String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.NUMBER, Integer::valueOf);
    }

    @GET
    @Path("/booleanParameters")
    @Operation(operationId = "getBoolean", description = "Get application parameter as Boolean by application id and parameter key")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Boolean.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getBoolean(@QueryParam("applicationId") String applicationId,
            @QueryParam("parameterKey") String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.BOOLEAN, Boolean::valueOf);
    }

    Response getValue(String applicationId, String parameterKey, ApplicationParameterTypeDTOV2 paramType,
            Function<String, Object> convert) {
        if (applicationId == null || applicationId.isEmpty() || parameterKey == null || parameterKey.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameters applicationId and parameterKey are mandatory.").build();
        }
        String type = paramType.name();
        List<ApplicationParameter> applicationParameterList = applicationParameterService
                .findByApplicationIdAndParameterAndTypeKeys(applicationId, parameterKey, type);
        if (!applicationParameterList.isEmpty()) {
            List<Object> results = applicationParameterList.stream().map(p -> convert.apply(p.getSetValue())).toList();
            return Response.ok(results).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
