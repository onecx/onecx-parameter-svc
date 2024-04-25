package org.tkit.onecx.parameters.domain.daos;

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
class ApplicationParameterDAOExceptionTest {

    @Inject
    ApplicationParameterDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void findApplicationParametersByKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.findApplicationParametersByKeys(null, null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_PARAMETER_BY_APPLICATION_ID_AND_PARAMETER_KEY_FAILED,
                exc.key);
    }

    @Test
    void findByApplicationIdAndParameterAndTypeKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class,
                () -> dao.findByApplicationIdAndParameterAndTypeKeys(null, null, null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_PARAMETERS_BY_APPLICATION_AND_PARAMETER_KEY_TYPE_FAILED,
                exc.key);
    }

    @Test
    void findByApplicationIdAndParameterKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class,
                () -> dao.findByApplicationIdAndParameterKeys(null, null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_PARAMETERS_BY_APPLICATION_AND_PARAMETER_KEYS_FAILED,
                exc.key);
    }

    @Test
    void findAllByApplicationIdTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.findAllByApplicationId(null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED, exc.key);
    }

    @Test
    void searchByCriteriaTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchByCriteria(null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_FAILED, exc.key);
    }

    @Test
    void searchAllApplicationsTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchAllApplications());
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_ALL_APPLICATIONS_FAILED, exc.key);
    }

    @Test
    void searchAllKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchAllKeys(null));
        Assertions.assertEquals(ApplicationParameterDAO.ErrorKeys.FIND_ALL_KEYS_FAILED, exc.key);
    }
}
