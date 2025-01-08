package org.tkit.onecx.parameters.domain.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * The parameter search criteria.
 */
@Getter
@Setter
public class ParameterSearchCriteria {

    /**
     * The application ID.
     */
    private String applicationId;

    /**
     * The product name
     */
    private String productName;

    /**
     * The parameter key.
     */
    private String name;

    private Integer pageNumber;

    private Integer pageSize;
}
