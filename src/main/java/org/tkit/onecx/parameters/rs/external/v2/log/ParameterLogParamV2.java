package org.tkit.onecx.parameters.rs.external.v2.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.parameters.rs.v2.model.ParametersBucketDTOV2;

@ApplicationScoped
public class ParameterLogParamV2 implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ParametersBucketDTOV2.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ParametersBucketDTOV2) x).getInstanceId()));
    }
}
