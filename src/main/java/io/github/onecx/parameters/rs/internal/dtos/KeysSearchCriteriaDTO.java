package io.github.onecx.parameters.rs.internal.dtos;

import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KeysSearchCriteriaDTO {

    @QueryParam("applicationId")
    @Schema(description = "The application parameter id.")
    private String applicationId;

}
