package org.tkit.onecx.parameters.domain.timer;

import static org.tkit.onecx.parameters.domain.timer.MaintenanceHistoryService.JOB_ID;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.daos.JobDAO;
import org.tkit.onecx.parameters.domain.models.Job;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MaintenanceHistoryServiceExceptionTest {

    @Inject
    MaintenanceHistoryService service;

    @InjectMock
    EntityManager em;

    @InjectMock
    JobDAO dao;

    @BeforeEach
    void beforeAll() {
        Mockito.when(dao.getJob(JOB_ID)).thenReturn(new Job());
        Mockito.when(em.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void testDaoException() {
        var exc = Assertions.assertThrows(Exception.class, () -> {
            service.maintenanceHistoryData();
        });
        var de = Assertions.assertInstanceOf(DAOException.class, exc);
        Assertions.assertEquals(ApplicationParameterHistoryDAO.ErrorKeys.DELETE_PARAMETER_HISTORY_OLDER_THAN_FAILED, de.key);
    }

}
