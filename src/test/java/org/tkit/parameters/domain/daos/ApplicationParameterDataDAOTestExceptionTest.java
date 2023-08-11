package org.tkit.parameters.domain.daos;

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
class ApplicationParameterDataDAOTestExceptionTest {

    @Inject
    ApplicationParameterDataDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void findApplicationParametersByKeysTest() {
        var exc = Assertions.assertThrows(DAOException.class, () -> dao.deleteByParameterId("id"));
        Assertions.assertEquals(ApplicationParameterDataDAO.Errors.FAILED_TO_DELETE_BY_GUID_QUERY,
                exc.key);
    }
}
