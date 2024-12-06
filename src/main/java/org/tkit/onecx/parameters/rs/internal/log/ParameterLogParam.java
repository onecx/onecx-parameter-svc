package org.tkit.onecx.parameters.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.parameters.rs.internal.model.*;

@ApplicationScoped
public class ParameterLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ParameterHistoryCriteriaDTO.class, x -> {
                    ParameterHistoryCriteriaDTO d = (ParameterHistoryCriteriaDTO) x;
                    return ParameterHistoryCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                            + d.getPageSize()
                            + "]";
                }),
                item(10, ParameterHistoryCountCriteriaDTO.class, x -> {
                    ParameterHistoryCountCriteriaDTO d = (ParameterHistoryCountCriteriaDTO) x;
                    return ParameterHistoryCountCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                            + d.getPageSize()
                            + "]";
                }),
                item(10, ParameterSearchCriteriaDTO.class, x -> {
                    ParameterSearchCriteriaDTO d = (ParameterSearchCriteriaDTO) x;
                    return ParameterSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }),
                item(10, ParameterCreateDTO.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ParameterCreateDTO) x).getKey()),
                item(10, ParameterUpdateDTO.class,
                        x -> x.getClass().getSimpleName()));
    }
}