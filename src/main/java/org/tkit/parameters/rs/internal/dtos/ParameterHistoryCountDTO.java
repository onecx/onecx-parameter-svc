package org.tkit.parameters.rs.internal.dtos;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ParameterHistoryCountDTO {

    private LocalDateTime creationDate;

    private Long count;

}
