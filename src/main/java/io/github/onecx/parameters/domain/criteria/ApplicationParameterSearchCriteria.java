package io.github.onecx.parameters.domain.criteria;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The application parameter search criteria.
 */
@Getter
@Setter
public class ApplicationParameterSearchCriteria {

    /**
     * The application ID.
     */
    private String applicationId;

    /**
     * The application parameter key.
     */
    private String key;

    private String name;

    private List<String> type;

    private Integer pageNumber;

    private Integer pageSize;
}
