package io.github.onecx.parameters.domain.di.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.parameters.di.v1.model.DataImportDTOV1;
import gen.io.github.onecx.parameters.di.v1.model.DataImportParamDTOV1;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameter;

/**
 * Import JSON format. Openapi: ./src/main/openapi/di-v1.yaml
 *
 * <pre>
 * {
 *   "appId" : {
 *     "key1": {
 *       "description": "description_1",
 *       "name": "name_1",
 *       "value": "value_1"
 *     },
 *     "key2": {
 *       "description": "description_2",
 *       "name": "name_2",
 *       "value": "value_2"
 *     }
 *   }
 * }
 * </pre>
 *
 * <pre>
 * operation:
 * - CLEAN_INSERT - delete all data and import new set
 * - UPDATE - update existing data from file or create new parameters
 * </pre>
 */
@DataImport("parameters")
public class ParameterDataImportService implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(ParameterDataImportService.class);

    public static final String KEY_SEPARATOR = "__";

    @Inject
    ApplicationParameterDAO dao;

    @Inject
    ObjectMapper mapper;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importData(DataImportConfig config) {
        log.info("Import parameters from configuration {}", config);
        try {
            String operation = config.getMetadata().getOrDefault("operation", "NONE");

            Consumer<DataImportDTOV1> action = switch (operation) {
                case "CLEAN_INSERT" -> this::cleanInsert;
                case "UPDATE" -> this::update;
                default -> null;
            };

            if (action == null) {
                log.warn("Not supported operation '{}' for the import configuration key '{}'", operation, config.getKey());
                return;
            }

            if (config.getData() == null || config.getData().length == 0) {
                log.warn("Import configuration key {} does not contains any data to import", config.getKey());
                return;
            }

            DataImportDTOV1 data = mapper.readValue(config.getData(), DataImportDTOV1.class);
            if (data.isEmpty()) {
                log.warn("Import configuration key {} does not contains any JSON data to import", config.getKey());
                return;
            }
            action.accept(data);
        } catch (Exception ex) {
            throw new ErrorImportException(ex);
        }
    }

    public static class ErrorImportException extends RuntimeException {
        public ErrorImportException(Exception ex) {
            super(ex.getMessage(), ex);
        }
    }

    private void cleanInsert(DataImportDTOV1 data) {

        // convert import model to list of parameters
        List<ApplicationParameter> params = new ArrayList<>();
        data.forEach((app, keys) -> {
            if (keys != null && !keys.isEmpty()) {
                keys.forEach((key, value) -> params.add(create(app, key, value)));
            }
        });

        // delete all parameters
        dao.deleteQueryAll();

        // create new parameters
        dao.create(params);
    }

    private void update(DataImportDTOV1 data) {
        Map<String, DataImportParamDTOV1> values = new HashMap<>();
        data.forEach((appId, app) -> {
            if (app != null) {
                app.forEach((paramId, param) -> values.put(id(appId, paramId), param));
            }
        });

        // update existing parameter
        Map<String, ApplicationParameter> params = dao.findApplicationParametersByKeys(values.keySet(), KEY_SEPARATOR);
        if (!params.isEmpty()) {
            List<ApplicationParameter> updated = new ArrayList<>();
            params.forEach((k, v) -> {
                updated.add(update(v, values.get(k)));
                values.remove(k);

            });
            dao.update(updated);
        }

        // no new application parameter to create
        if (values.isEmpty()) {
            return;
        }

        // create new parameter
        List<ApplicationParameter> created = new ArrayList<>();
        values.forEach((k, value) -> {
            String[] tmp = k.split(KEY_SEPARATOR);
            created.add(create(tmp[0], tmp[1], value));
        });
        dao.create(created);
    }

    private ApplicationParameter update(ApplicationParameter param, DataImportParamDTOV1 value) {
        if (isValue(value.getName())) {
            param.setName(value.getName());
        }
        if (isValue(value.getDescription())) {
            param.setDescription(value.getDescription());
        }
        if (isValue(value.getValue())) {
            param.setImportValue(value.getValue());
        }
        return param;
    }

    private ApplicationParameter create(String app, String key, DataImportParamDTOV1 value) {
        ApplicationParameter param = new ApplicationParameter();
        param.setApplicationId(app);
        param.setKey(key);
        param.setName(value.getName());
        param.setDescription(value.getDescription());
        param.setImportValue(value.getValue());
        return param;
    }

    private boolean isValue(String value) {
        return value != null && !value.isBlank();
    }

    private String id(String app, String key) {
        return app + KEY_SEPARATOR + key;
    }
}
