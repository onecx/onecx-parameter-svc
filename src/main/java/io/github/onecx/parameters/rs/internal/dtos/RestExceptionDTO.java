package io.github.onecx.parameters.rs.internal.dtos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class RestExceptionDTO {

    /**
     * The error code.
     */
    private String errorCode;

    /**
     * The message.
     */
    private String message;

    /**
     * The error parameters.
     */
    private List<Object> parameters;

    /**
     * The named parameters.
     */
    private Map<String, Object> namedParameters = new HashMap<>();
}
