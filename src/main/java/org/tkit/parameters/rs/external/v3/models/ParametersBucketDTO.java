package org.tkit.parameters.rs.external.v3.models;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ParametersBucketDTO {

    private Map<String, ParameterInfoDTO> parameters = new ConcurrentHashMap<>();

    private String instanceId;

    private OffsetDateTime start;

    private OffsetDateTime end;

}
