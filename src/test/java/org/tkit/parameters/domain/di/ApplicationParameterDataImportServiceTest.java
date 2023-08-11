package org.tkit.parameters.domain.di;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.parameters.domain.daos.ApplicationParameterDAO;
import org.tkit.parameters.domain.di.models.ApplicationParameterDataImport;
import org.tkit.parameters.domain.models.ApplicationParameter;
import org.tkit.parameters.test.AbstractTest;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class ApplicationParameterDataImportServiceTest extends AbstractTest {

    @Inject
    ApplicationParameterDataImportService service;

    @Inject
    ApplicationParameterDAO dao;

    @Inject
    ObjectMapper mapper;

    @Test
    void importNoneTest() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("operation", "CUSTOM_NOT_SUPPORTED");
        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return metadata;
            }
        };
        service.importData(config);

        List<ApplicationParameter> params = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(params);
        Assertions.assertEquals(9, params.size());

        config.getMetadata().put("operation", "NONE");

        params = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(params);
        Assertions.assertEquals(9, params.size());
    }

    @Test
    void importCleanInsertTest() {

        ApplicationParameterDataImport param = new ApplicationParameterDataImport();
        param.setDescription("desc");
        param.setName("name-of-name");

        var data = new HashMap<String, Map<String, ApplicationParameterDataImport>>();
        data.put("app1", Map.of("key1", param));
        data.put("empty", Map.of());
        data.put("null", null);

        service.importData(new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "CLEAN_INSERT");
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        List<ApplicationParameter> params = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(params);
        Assertions.assertEquals(1, params.size());

    }

    @Test
    void importUpdateTest() {

        ApplicationParameterDataImport param = new ApplicationParameterDataImport();
        param.setDescription("desc");
        param.setName("name-of-name");
        param.setValue("123");

        ApplicationParameterDataImport emptyValueParam = new ApplicationParameterDataImport();
        emptyValueParam.setDescription("");
        emptyValueParam.setName("");
        emptyValueParam.setValue("");

        var data = new HashMap<String, Map<String, ApplicationParameterDataImport>>();
        data.put("app1",
                Map.of(
                        "param", param,
                        "integer_param", new ApplicationParameterDataImport(),
                        "boolean_param", emptyValueParam));
        data.put("app_new", Map.of("key_new", param));
        data.put("empty", Map.of());
        data.put("null", null);

        service.importData(new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "UPDATE");
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper
                            .writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        List<ApplicationParameter> params = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(params);
        Assertions.assertEquals(10, params.size());
        List<ApplicationParameter> p1 = dao.findByApplicationIdAndParameterKeys("app1", List.of("param"));
        Assertions.assertNotNull(p1);
        Assertions.assertEquals(1, p1.size());
        Assertions.assertEquals(param.getName(), p1.get(0).getName());
        Assertions.assertEquals(param.getValue(), p1.get(0).getImportValue());

        List<ApplicationParameter> p2 = dao.findByApplicationIdAndParameterKeys("app_new", List.of("key_new"));
        Assertions.assertNotNull(p2);
        Assertions.assertEquals(1, p2.size());
        Assertions.assertEquals(param.getName(), p2.get(0).getName());
        Assertions.assertEquals(param.getValue(), p2.get(0).getImportValue());
    }

    @Test
    void importUpdateNoDataToUpdateTest() {

        var data = new HashMap<String, Map<String, ApplicationParameterDataImport>>();
        data.put("empty", Map.of());
        data.put("null", null);

        service.importData(new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "UPDATE");
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper
                            .writeValueAsBytes(data);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        List<ApplicationParameter> params = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(params);
        Assertions.assertEquals(9, params.size());
        List<ApplicationParameter> p1 = dao.findByApplicationIdAndParameterKeys("app1", List.of("param"));
        Assertions.assertNotNull(p1);
        Assertions.assertEquals(1, p1.size());

    }

    @Test
    void importEmptyDataTest() {
        Assertions.assertDoesNotThrow(() -> {
            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "UPDATE");
                }
            });

            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "UPDATE");
                }

                @Override
                public byte[] getData() {
                    return new byte[] {};
                }
            });

            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("operation", "UPDATE");
                }

                @Override
                public byte[] getData() {
                    try {
                        return mapper.writeValueAsBytes(Map.of());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

        });

        var config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("operation", "UPDATE");
            }

            @Override
            public byte[] getData() {
                return new byte[] { 0 };
            }
        };
        Assertions.assertThrows(RuntimeException.class, () -> service.importData(config));

    }
}
