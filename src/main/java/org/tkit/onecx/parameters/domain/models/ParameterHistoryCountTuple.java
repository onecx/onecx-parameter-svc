package org.tkit.onecx.parameters.domain.models;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ParameterHistoryCountTuple {

    private LocalDateTime creationDate;

    private Long count;

    public ParameterHistoryCountTuple(LocalDateTime creationDate, Long count) {
        this.creationDate = creationDate;
        this.count = count;
    }
}
