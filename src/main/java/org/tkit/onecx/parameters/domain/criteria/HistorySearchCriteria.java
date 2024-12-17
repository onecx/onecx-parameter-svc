package org.tkit.onecx.parameters.domain.criteria;

import java.util.List;

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

    private List<String> type;

    private Integer pageNumber;

    private Integer pageSize;
}
