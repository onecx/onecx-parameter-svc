package org.tkit.onecx.parameters.domain.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.daos.ParameterDAO;
import org.tkit.onecx.parameters.domain.models.Parameter;

@ApplicationScoped
public class ParameterService {

    @Inject
    ParameterDAO dao;

    @Transactional
    public void importParameters(List<Parameter> create, List<Parameter> update) {
        dao.create(create);
        dao.update(update);
    }
}
