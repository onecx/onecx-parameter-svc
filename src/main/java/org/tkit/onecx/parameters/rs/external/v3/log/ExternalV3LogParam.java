package org.tkit.onecx.parameters.rs.external.v3.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.parameters.rs.v3.model.ParametersBucketDTOV3;

@ApplicationScoped
public class ExternalV3LogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ParametersBucketDTOV3.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ParametersBucketDTOV3) x).getInstanceId()));
    }
}
