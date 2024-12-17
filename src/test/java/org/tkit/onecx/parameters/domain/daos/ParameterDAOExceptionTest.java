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
class ParameterDAOExceptionTest {

    @Inject
    ParameterDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void findAllByApplicationIdTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.findAllByProductNameAndApplicationId(null, null));
        Assertions.assertEquals(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED, exc.key);
    }

    @Test
    void searchByCriteriaTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchByCriteria(null));
        Assertions.assertEquals(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_FAILED, exc.key);
    }

    @Test
    void searchAllApplicationsTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchAllProductNamesAndApplicationIds());
        Assertions.assertEquals(ParameterDAO.ErrorKeys.FIND_ALL_APPLICATIONS_FAILED, exc.key);
    }

    @Test
    void searchAllKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.searchAllNames(null));
        Assertions.assertEquals(ParameterDAO.ErrorKeys.FIND_ALL_NAMES_FAILED, exc.key);
    }
}
