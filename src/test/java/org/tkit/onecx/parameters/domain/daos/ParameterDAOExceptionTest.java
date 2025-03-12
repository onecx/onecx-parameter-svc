package org.tkit.onecx.parameters.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
    void test() {
        test(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED,
                () -> dao.findAllByProductNameAndApplicationId(null, null));
        test(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_VALUES_BY_APPLICATION_ID_FAILED,
                () -> dao.findAllValuesByProductNameAndApplicationId(null, null));
        test(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_FAILED, () -> dao.searchByCriteria(null));
        test(ParameterDAO.ErrorKeys.FIND_ALL_APPLICATIONS_FAILED, () -> dao.searchAllProductNamesAndApplicationIds());
        test(ParameterDAO.ErrorKeys.FIND_ALL_NAMES_FAILED, () -> dao.searchAllNames(null));
        test(ParameterDAO.ErrorKeys.FIND_ALL_PARAMETERS_BY_PRODUCT_NAMES_FAILED, () -> dao.findAllByProductNames(null));
        test(ParameterDAO.ErrorKeys.FIND_BY_NAME_PRODUCT_NAME_APPLICATION_ID_FAILED,
                () -> dao.findByNameApplicationIdAndProductName(null, null, null));
    }

    private void test(Enum<?> key, Executable executable) {
        var exc = Assertions.assertThrows(DAOException.class, executable);
        Assertions.assertEquals(key, exc.key);
    }

}
