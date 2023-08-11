package org.tkit.parameters.rs.external.v2.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractEntityDTOV2 {

    /**
     * The persisted flag.
     */
    private boolean persisted;

    /**
     * The ID.
     */
    private String id;

    /**
     * The optimistic lock version
     */
    private Integer version;

}
