package org.tkit.onecx.parameters.domain.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ApplicationTuple(String productName, String appId) {
}