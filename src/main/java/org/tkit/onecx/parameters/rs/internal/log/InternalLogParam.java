package org.tkit.onecx.parameters.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tki.onecx.parameters.rs.internal.model.ApplicationParameterCreateDTO;
import gen.org.tki.onecx.parameters.rs.internal.model.ApplicationParameterUpdateDTO;

@ApplicationScoped
public class InternalLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ApplicationParameterCreateDTO.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ApplicationParameterCreateDTO) x).getKey()),
                item(10, ApplicationParameterUpdateDTO.class,
                        x -> x.getClass().getSimpleName() + ":" + ((ApplicationParameterUpdateDTO) x).getUnit()));
    }
}
