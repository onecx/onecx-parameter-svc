package org.tkit.onecx.parameters.domain.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * The application parameter search criteria.
 */
@Getter
@Setter
public class HistorySearchCriteria {

    /**
     * The application ID.
     */
    private String applicationId;

    /**
     * The product name
     */
    private String productName;

    /**
     * The parameter name.
     */
    private String name;

    private Integer pageNumber;

    private Integer pageSize;
}
