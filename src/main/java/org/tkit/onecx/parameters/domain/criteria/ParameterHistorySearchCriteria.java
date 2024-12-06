package org.tkit.onecx.parameters.domain.criteria;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The application parameter search criteria.
 */
@Getter
@Setter
public class ParameterHistorySearchCriteria {

    /**
     * The application ID.
     */
    private String applicationId;

    /**
     * The product name
     */
    private String productName;

    /**
     * The application parameter key.
     */
    private String key;

    private List<String> type;

    private Integer pageNumber;

    private Integer pageSize;
}
