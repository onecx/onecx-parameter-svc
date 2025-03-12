package org.tkit.onecx.parameters.rs.operator.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.parameters.rs.v1.operator.model.ParametersUpdateRequestOperatorDTOV1;

@ApplicationScoped
public class OperatorParameterLogParamV1 implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ParametersUpdateRequestOperatorDTOV1.class,
                        x -> ParametersUpdateRequestOperatorDTOV1.class.getSimpleName() + "["
                                + ((ParametersUpdateRequestOperatorDTOV1) x).getParameters().size() + "]"));
    }
}
