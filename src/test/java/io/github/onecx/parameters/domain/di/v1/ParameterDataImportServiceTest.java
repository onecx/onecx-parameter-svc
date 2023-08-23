package io.github.onecx.parameters.domain.di.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.parameters.di.v1.model.DataImportAppDTOV1;
import gen.io.github.onecx.parameters.di.v1.model.DataImportDTOV1;
import gen.io.github.onecx.parameters.di.v1.model.DataImportParamDTOV1;
import io.github.onecx.parameters.domain.daos.ApplicationParameterDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.github.onecx.parameters.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = { "data/parameters-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class ParameterDataImportServiceTest extends AbstractTest {

    @Inject
    ParameterDataImportService service;

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

        DataImportParamDTOV1 k1 = new DataImportParamDTOV1();
        k1.setDescription("desc");
        k1.setName("name-of-name");

        DataImportAppDTOV1 a1 = new DataImportAppDTOV1();
        a1.put("key1", k1);

        DataImportDTOV1 data = new DataImportDTOV1();
        data.put("app1", a1);
        data.put("empty", new DataImportAppDTOV1());
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

        DataImportParamDTOV1 param = new DataImportParamDTOV1();
        param.setDescription("desc");
        param.setName("name-of-name");
        param.setValue("123");

        DataImportParamDTOV1 emptyValueParam = new DataImportParamDTOV1();
        emptyValueParam.setDescription("");
        emptyValueParam.setName("");
        emptyValueParam.setValue("");

        DataImportAppDTOV1 appNew = new DataImportAppDTOV1();
        appNew.put("key_new", param);

        DataImportAppDTOV1 app1 = new DataImportAppDTOV1();
        app1.put("param", param);
        app1.put("integer_param", new DataImportParamDTOV1());
        app1.put("boolean_param", emptyValueParam);

        DataImportDTOV1 data = new DataImportDTOV1();
        data.put("app1", app1);
        data.put("app_new", appNew);
        data.put("empty", new DataImportAppDTOV1());
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

        DataImportDTOV1 data = new DataImportDTOV1();
        data.put("empty", new DataImportAppDTOV1());
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
