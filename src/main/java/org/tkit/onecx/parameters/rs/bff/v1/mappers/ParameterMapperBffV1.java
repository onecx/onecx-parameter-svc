package org.tkit.onecx.parameters.rs.bff.v1.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.parameters.domain.models.Parameter;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParameterBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkRequestBffDTOV1;
import gen.org.tkit.onecx.parameters.rs.v1.bff.model.ParametersBulkResponseBffDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface ParameterMapperBffV1 {

    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "applicationId", target = "applicationId")
    ParameterBffDTOV1 parameterToParameterBffDTOV1(Parameter parameter);

    default ParametersBulkResponseBffDTOV1 mapParameters(List<Parameter> parameters, ParametersBulkRequestBffDTOV1 request) {
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
                                                .collect(Collectors.toList())))));
        bulkResponseBffDTOV1.setProducts(productsMap);
        return bulkResponseBffDTOV1;
    }
}
