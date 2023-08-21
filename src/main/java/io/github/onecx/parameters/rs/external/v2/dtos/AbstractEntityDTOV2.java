package io.github.onecx.parameters.rs.external.v2.dtos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AbstractEntityDTOV2 {

    /**
     * The persisted flag.
     */
    private boolean persisted;

    /**
     * The ID.
     */
    @EqualsAndHashCode.Include
    private String id;

    /**
     * The optimistic lock version
     */
    private Integer version;

}
