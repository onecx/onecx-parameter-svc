package io.github.onecx.parameters.rs.internal;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import gen.io.github.onecx.parameters.rs.internal.ParametersApi;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterCreateDTO;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterDTO;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterPageResultDTO;
import gen.io.github.onecx.parameters.rs.internal.model.ApplicationParameterUpdateDTO;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDataDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.github.onecx.parameters.domain.models.ApplicationParameterData;
import io.github.onecx.parameters.rs.internal.mappers.ApplicationParameterDataMapper;
import io.github.onecx.parameters.rs.internal.mappers.ApplicationParameterInternalMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ApplicationParameterRestController implements ParametersApi {

    @Inject
    ApplicationParameterDAO applicationParameterDAO;

    @Inject
    ApplicationParameterDataDAO applicationParameterDataDAO;

    @Inject
    ApplicationParameterInternalMapper applicationParameterInternalMapper;

    @Inject
    ApplicationParameterDataMapper applicationParameterDataMapper;

    @Context
    UriInfo uriInfo;

    @Override
    public Response getAllApplications() {
        var apps = applicationParameterDAO.searchAllApplications();
        return Response.ok(applicationParameterInternalMapper.apps(apps)).build();
    }

    @Override
    public Response getAllKeys(String applicationId) {
        var criteria = applicationParameterInternalMapper.map(applicationId);
        var keys = applicationParameterDAO.searchAllKeys(criteria);
        return Response.ok(applicationParameterInternalMapper.keys(keys)).build();
    }

    @Override
    public Response getAllApplicationParameters(String applicationId, String key, String name, Integer pageNumber,
            Integer pageSize, List<String> type) {

        var criteria = applicationParameterInternalMapper.map(applicationId, key, name, pageNumber, pageSize, type);
        var parameters = applicationParameterDAO.searchByCriteria(criteria);
        ApplicationParameterPageResultDTO results = applicationParameterInternalMapper.map(parameters);

        // map ApplicationParameterData to ApplicationParameter due to backward compatibility
        if (!results.getStream().isEmpty()) {
            List<ApplicationParameterDTO> data = results.getStream();
            var parametersData = applicationParameterDataDAO
                    .findByParameterIds(data.stream().map(ApplicationParameterDTO::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(ApplicationParameterData::getApplicationParameterGuid, d -> d));

            data.forEach(p -> applicationParameterDataMapper.map(parametersData.get(p.getId()), p));

            results.setStream(data);
        }

        return Response.ok(results).build();
    }

    @Override
    public Response getParameterById(String id) {
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

    @Override
    @Transactional
    public Response updateParameterValue(String id,
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

    @Override
    @Transactional
    public Response createParameterValue(ApplicationParameterCreateDTO request) {

        ApplicationParameter param = applicationParameterDataMapper.create(request);
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

    @Override
    @Transactional
    public Response deleteParameter(String id) {
        ApplicationParameter parameter = applicationParameterDAO.findById(id);
        if (parameter != null) {
            applicationParameterDAO.delete(parameter);
            // ApplicationParameterData deletion due to backward compatibility
            applicationParameterDataDAO.deleteByParameterId(parameter.getId());
        }
        return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
    }

}
