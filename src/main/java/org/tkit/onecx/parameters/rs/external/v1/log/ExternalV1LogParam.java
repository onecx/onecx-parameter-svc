package org.tkit.onecx.parameters.rs.external.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.parameters.rs.v1.model.ParametersBucketDTOV1;

@ApplicationScoped
public class ExternalV1LogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ParametersBucketDTOV1.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ParametersBucketDTOV1) x).getInstanceId()));
    }
}
