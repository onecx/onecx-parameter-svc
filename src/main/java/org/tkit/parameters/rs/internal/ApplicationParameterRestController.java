package org.tkit.parameters.rs.internal;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.tkit.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.parameters.domain.daos.ApplicationParameterDataDAO;
import org.tkit.parameters.domain.models.ApplicationParameter;
import org.tkit.parameters.domain.models.ApplicationParameterData;
import org.tkit.parameters.rs.internal.dtos.*;
import org.tkit.parameters.rs.internal.mappers.ApplicationParameterDataMapper;
import org.tkit.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Tag(name = "internal")
@Path("/parameters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterRestController {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterDataDAO applicationParameterDataDAO;

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Inject
    ApplicationParameterDataMapper applicationParameterDataMapper;

    @GET()
    @Path("applications")
    @Operation(operationId = "getAllApplications", description = "Find all parameters")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationsPageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllApplications() {
        var apps = applicationParameterDAO.searchAllApplications();
        return Response.ok(applicationParameterInternalMapper.apps(apps)).build();
    }

    @GET()
    @Path("keys")
    @Operation(operationId = "getAllKeys", description = "Find all parameters")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = KeysPageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllKeys(@BeanParam KeysSearchCriteriaDTO dto) {
        var criteria = applicationParameterInternalMapper.map(dto);
        var keys = applicationParameterDAO.searchAllKeys(criteria);
        return Response.ok(applicationParameterInternalMapper.apps(keys)).build();
    }

    @GET
    @Operation(operationId = "getAllApplicationParameters", description = "Find all parameters")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterPageResultDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getAllApplicationParameters(@BeanParam ApplicationParameterSearchCriteriaDTO dto) {
        var criteria = applicationParameterInternalMapper.map(dto);
        var parameters = applicationParameterDAO.searchByCriteria(criteria);
        ApplicationParameterPageResultDTO results = applicationParameterInternalMapper.map(parameters);

        // map ApplicationParameterData to ApplicationParameter due to backward compatibility
        if (!results.getStream().isEmpty()) {
            List<ApplicationParameterDTO> data = results.getStream();
            var parametersData = applicationParameterDataDAO.findByParameterIds(data.stream().map(TraceableDTO::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(ApplicationParameterData::getApplicationParameterGuid, d -> d));

            data.forEach(p -> applicationParameterDataMapper.map(parametersData.get(p.getId()), p));

            results.setStream(data);
        }

        return Response.ok(results).build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getParameterById", description = "Find parameter by id")
    @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApplicationParameterDTO.class)))
    @APIResponse(responseCode = "400", description = "Bad request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response getParameterById(@PathParam("id") String id) {
        ApplicationParameter param = applicationParameterDAO.findById(id);
        if (param == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(applicationParameterInternalMapper.createRestException(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        ApplicationParameterDTO parameterDTO = applicationParameterInternalMapper.map(param);

        // map ApplicationParameterData to ApplicationParameter due to backward compatibility
        ApplicationParameterData parameterData = applicationParameterDataDAO.findByParameterId(id);
        applicationParameterDataMapper.map(parameterData, parameterDTO);

        return Response.ok(parameterDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(operationId = "updateParameterValue", description = "Update parameter")
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestExceptionDTO.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response updateParameterValue(@PathParam("id") String id,
            ApplicationParameterUpdateDTO applicationParameterUpdateDTO) {
        ApplicationParameter applicationParameter = applicationParameterDAO.findById(id);
        if (applicationParameter == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(applicationParameterInternalMapper.createRestException(Response.Status.NOT_FOUND.name(),
                            "Parameter with id" + id + " not found."))
                    .build();
        }
        applicationParameterInternalMapper.update(applicationParameterUpdateDTO, applicationParameter);
        applicationParameter = applicationParameterDAO.update(applicationParameter);

        // ApplicationParameterData update/creation due to backward compatibility
        if ((applicationParameterUpdateDTO.getUnit() != null && !applicationParameterUpdateDTO.getUnit().isBlank())
                || applicationParameterUpdateDTO.getRangeFrom() != null
                || applicationParameterUpdateDTO.getRangeTo() != null) {
            ApplicationParameterData applicationParameterData = applicationParameterDataDAO
                    .findByParameterId(applicationParameter.getId());
            if (applicationParameterData != null) {
                applicationParameterData = applicationParameterDataMapper.update(applicationParameterUpdateDTO,
                        applicationParameterData);
                applicationParameterDataDAO.update(applicationParameterData);
            } else {
                applicationParameterData = applicationParameterDataMapper.create(applicationParameterUpdateDTO,
                        applicationParameter.getId());
                applicationParameterDataDAO.create(applicationParameterData);
            }
        }

        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

    @POST
    @Transactional
    @Operation(operationId = "createParameterValue", description = "Create parameter")
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestExceptionDTO.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response createParameterValue(ApplicationParameterCreateDTO request, @Context UriInfo uriInfo) {
        ApplicationParameter param = new ApplicationParameter();
        param.setApplicationId(request.getApplicationId());
        param.setKey(request.getKey());
        param.setType(request.getType());
        param.setDescription(request.getDescription());
        param.setSetValue(request.getValue());
        param = applicationParameterDAO.create(param);

        // ApplicationParameterData creation due to backward compatibility
        if ((request.getUnit() != null && !request.getUnit().isBlank())
                || request.getRangeFrom() != null
                || request.getRangeTo() != null) {
            ApplicationParameterData applicationParameterData = applicationParameterDataMapper.create(request, param.getId());
            applicationParameterDataDAO.create(applicationParameterData);
        }

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(param.getId()).build())
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(operationId = "deleteParameter", description = "Delete parameter")
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestExceptionDTO.class)))
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    public Response deleteParameter(@PathParam("id") String id) {
        ApplicationParameter parameter = applicationParameterDAO.findById(id);
        if (parameter != null) {
            applicationParameterDAO.delete(parameter);
            // ApplicationParameterData deletion due to backward compatibility
            applicationParameterDataDAO.deleteByParameterId(parameter.getId());
        }
        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

}
