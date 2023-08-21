package io.github.onecx.parameters.domain.timer;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import io.github.onecx.parameters.domain.daos.JobDAO;
import io.github.onecx.parameters.domain.models.ApplicationParameterHistory;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = { "data/history-testdata.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
class MaintenanceHistoryServiceTest {

    @Inject
    MaintenanceHistoryService service;

    @Inject
    ApplicationParameterHistoryDAO dao;

    @Inject
    JobDAO jobDAO;

    @Test
    @Order(1)
    void maintenanceHistoryDataTest() {
        service.maintenanceHistoryData();
        List<ApplicationParameterHistory> result = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Order(2)
    void maintenanceHistoryNoDataTest() {
        var result = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());

        jobDAO.deleteQueryById(MaintenanceHistoryService.JOB_ID);
        service.maintenanceHistoryData();

        result = dao.findAll().collect(Collectors.toList());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.size());
    }

}
