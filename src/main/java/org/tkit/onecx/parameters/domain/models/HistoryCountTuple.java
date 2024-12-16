package org.tkit.onecx.parameters.domain.models;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class HistoryCountTuple {

    private LocalDateTime creationDate;

    private Long count;

    public HistoryCountTuple(LocalDateTime creationDate, Long count) {
        this.creationDate = creationDate;
        this.count = count;
    }
}
