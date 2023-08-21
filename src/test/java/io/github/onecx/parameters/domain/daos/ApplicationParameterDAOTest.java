package io.github.onecx.parameters.domain.daos;

import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.parameters.domain.di.ApplicationParameterDataImportService;
import io.github.onecx.parameters.domain.models.ApplicationParameter;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ApplicationParameterDAOTest {

    @Inject
    ApplicationParameterDAO dao;

    @Test
    @WithDBData(value = { "data/parameters-dao-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void findApplicationParametersByKeysTest() {
        Set<String> data = Set.of("app1__param", "app1__integer_param");
        Map<String, ApplicationParameter> result = dao.findApplicationParametersByKeys(data,
                ApplicationParameterDataImportService.KEY_SEPARATOR);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @WithDBData(value = { "data/parameters-dao-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
    void findApplicationParametersByKeysNoResultTest() {
        Set<String> data = Set.of("appx__paramx");
        Map<String, ApplicationParameter> result = dao.findApplicationParametersByKeys(data,
                ApplicationParameterDataImportService.KEY_SEPARATOR);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }
}
