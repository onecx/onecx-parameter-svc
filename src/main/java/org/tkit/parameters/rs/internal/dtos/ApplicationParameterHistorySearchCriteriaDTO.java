package org.tkit.parameters.rs.internal.dtos;

import java.util.List;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationParameterHistorySearchCriteriaDTO {

    @QueryParam("applicationId")
    @Schema(description = "The application parameter id.")
    private String applicationId;

    @QueryParam("key")
    @Schema(description = "The application parameter key.")
    private String key;

    @QueryParam("type")
    @Schema(description = "The application parameter type.", implementation = String.class, type = SchemaType.ARRAY)
    private List<String> type;

    @DefaultValue("0")
    @QueryParam("pageNumber")
    @Schema(description = "The number of page.")
    private Integer pageNumber = 0;

    @DefaultValue("100")
    @QueryParam("pageSize")
    @Schema(description = "The size of page")
    private Integer pageSize = 100;
}
