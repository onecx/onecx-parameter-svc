package io.github.onecx.parameters.domain.di;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.di.models.ApplicationParameterDataImport;
import io.github.onecx.parameters.domain.models.ApplicationParameter;

/**
 * Import format
 * {
 * "appId" : {
 * "key1": {
 * "description": "description_1",
 * "name": "name_1",
 * "value": "value_1"
 * },
 * "key2": {
 * "description": "description_2",
 * "name": "name_2",
 * "value": "value_2"
 * }
 * }
 * }
 * operation:
 * - CLEAN_INSERT - delete all data and import new set
 * - UPDATE - update existing data from file or create new parameters
 */
@DataImport("parameters")
public class ApplicationParameterDataImportService implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationParameterDataImportService.class);

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

            Consumer<Map<String, Map<String, ApplicationParameterDataImport>>> action = switch (operation) {
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

            Map<String, Map<String, ApplicationParameterDataImport>> data = mapper.readValue(config.getData(),
                    new TypeReference<Map<String, Map<String, ApplicationParameterDataImport>>>() {
                    });
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

    private void cleanInsert(Map<String, Map<String, ApplicationParameterDataImport>> data) {

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

    private void update(Map<String, Map<String, ApplicationParameterDataImport>> data) {
        Map<String, ApplicationParameterDataImport> values = new HashMap<>();
        data.forEach((app, keys) -> {
            if (keys != null && !keys.isEmpty()) {
                keys.forEach((key, value) -> values.put(id(app, key), value));
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

    private ApplicationParameter update(ApplicationParameter param, ApplicationParameterDataImport value) {
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

    private ApplicationParameter create(String app, String key, ApplicationParameterDataImport value) {
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
