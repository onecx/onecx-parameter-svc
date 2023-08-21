package io.github.onecx.parameters.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ApplicationParameterHistoryDAOExceptionTest {

    @Inject
    ApplicationParameterHistoryDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void searchByCriteriaTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchByCriteria(null));
        Assertions.assertEquals(ApplicationParameterHistoryDAO.ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED,
                exc.key);
    }

    @Test
    void searchOnlyLatestByCriteriaTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchOnlyLatestByCriteria(null));
        Assertions.assertEquals(ApplicationParameterHistoryDAO.ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED,
                exc.key);
    }

    @Test
    void searchCountsByCriteriaTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchCountsByCriteria(null));
        Assertions.assertEquals(ApplicationParameterHistoryDAO.ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED,
                exc.key);
    }
}
