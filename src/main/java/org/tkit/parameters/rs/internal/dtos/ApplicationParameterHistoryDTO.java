package org.tkit.parameters.rs.internal.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
@SuppressWarnings("java:S2160")
public class ApplicationParameterHistoryDTO extends TraceableDTO {

    private String applicationId;

    private String key;

    private String usedValue;

    private String defaultValue;

    private String type;

    private String instanceId;
}
