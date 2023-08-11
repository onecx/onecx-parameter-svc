package org.tkit.parameters.rs.external.v3.models;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersBucketDTO {

    public Map<String, ParameterInfoDTO> parameters = new ConcurrentHashMap<>();

    public String instanceId;

    public OffsetDateTime start;

    public OffsetDateTime end;

}
