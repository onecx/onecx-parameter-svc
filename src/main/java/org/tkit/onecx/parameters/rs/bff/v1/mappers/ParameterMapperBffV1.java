package org.tkit.onecx.parameters.rs.bff.v1.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.onecx.parameters.rs.internal.mappers.ParameterMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParameterBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkRequestBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkResponseBffDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class ParameterMapperBffV1 {
    @Inject
    ObjectMapper objectMapper;

    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "applicationId", target = "applicationId")
    @Mapping(target = "value", source = "value", qualifiedByName = "s2o")
    @Mapping(target = "importValue", source = "importValue", qualifiedByName = "s2o")
    public abstract ParameterBffDTOV1 parameterToParameterBffDTOV1(Parameter parameter);

    public ParametersBulkResponseBffDTOV1 mapParameters(List<Parameter> parameters, ParametersBulkRequestBffDTOV1 request) {
        ParametersBulkResponseBffDTOV1 bulkResponseBffDTOV1 = new ParametersBulkResponseBffDTOV1();
        Map<String, Map<String, List<ParameterBffDTOV1>>> productsMap = request.getProducts().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.toMap(
                                        applicationId -> applicationId,
                                        applicationId -> parameters.stream()
                                                .filter(p -> p.getProductName().equals(entry.getKey())
                                                        && p.getApplicationId().equals(applicationId))
                                                .map(this::parameterToParameterBffDTOV1)
                                                .toList()))));
        bulkResponseBffDTOV1.setProducts(productsMap);
        return bulkResponseBffDTOV1;
    }

    @Named("s2o")
    public Object s2o(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new ParameterMapper.MapperException("Error reading parameter value", e);
        }
    }
}
