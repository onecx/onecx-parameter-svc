package io.github.onecx.parameters.rs.external.v2;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.parameters.rs.v2.ExternalApi;
import gen.io.github.onecx.parameters.rs.v2.model.ApplicationParameterDTOV2;
import gen.io.github.onecx.parameters.rs.v2.model.ApplicationParameterTypeDTOV2;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDataDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.github.onecx.parameters.domain.models.ApplicationParameterData;
import io.github.onecx.parameters.rs.external.v2.mappers.ApplicationParameterDataMapperV2;
import io.github.onecx.parameters.rs.external.v2.mappers.ApplicationParameterMapperV2;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ParameterRestControllerV2 implements ExternalApi {

    @Inject
    ApplicationParameterDAO applicationParameterService;

    @Inject
    ApplicationParameterDataDAO applicationParameterDataDAO;

    @Inject
    ApplicationParameterMapperV2 extApplicationParameterMapperV2;

    @Inject
    ApplicationParameterDataMapperV2 applicationParameterDataMapperV2;

    @Override
    public Response getParameter(String applicationId, String parameterKey) {
        if (applicationId == null || applicationId.isEmpty() || parameterKey == null || parameterKey.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ApplicationId and parameterKey are mandatory.").build();
        }

        var applicationParameterList = applicationParameterService
                .findByApplicationIdAndParameterKeys(applicationId, List.of(parameterKey));
        return getAllParameters(applicationParameterList);
    }

    @Override
    public Response getAllParameters(String applicationId, List<String> parametersKeys) {
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
                    .findByParameterIds(results.stream().map(ApplicationParameterDTOV2::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(ApplicationParameterData::getApplicationParameterGuid, d -> d));

            results.forEach(p -> applicationParameterDataMapperV2.map(parametersData.get(p.getId()), p));

            return Response.ok(results).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response getString(String applicationId, String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.STRING, s -> s);
    }

    @Override
    public Response getLong(String applicationId, String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.NUMBER, Long::valueOf);
    }

    @Override
    public Response getInteger(String applicationId, String parameterKey) {
        return getValue(applicationId, parameterKey, ApplicationParameterTypeDTOV2.NUMBER, Integer::valueOf);
    }

    @Override
    public Response getBoolean(String applicationId, String parameterKey) {
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
