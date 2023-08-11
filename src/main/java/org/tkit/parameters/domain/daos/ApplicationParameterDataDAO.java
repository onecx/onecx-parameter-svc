package org.tkit.parameters.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.tkit.parameters.domain.models.ApplicationParameterData;
import org.tkit.parameters.domain.models.ApplicationParameterData_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class ApplicationParameterDataDAO extends AbstractDAO<ApplicationParameterData> {

    public ApplicationParameterData findByParameterId(String parameterId) throws DAOException {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ApplicationParameterData> cq = cb.createQuery(ApplicationParameterData.class);
        Root<ApplicationParameterData> root = cq.from(ApplicationParameterData.class);

        cq.select(root);
        cq.where(cb.equal(root.get(ApplicationParameterData_.APPLICATION_PARAMETER_GUID), parameterId));

        try {
            return this.em.createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ApplicationParameterData> findByParameterIds(List<String> parameterIds) throws DAOException {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ApplicationParameterData> cq = cb.createQuery(ApplicationParameterData.class);
        Root<ApplicationParameterData> root = cq.from(ApplicationParameterData.class);

        cq.select(root);
        cq.where(root.get(ApplicationParameterData_.APPLICATION_PARAMETER_GUID).in(parameterIds));

        return this.em.createQuery(cq).getResultList();
    }

    @Transactional(value = Transactional.TxType.SUPPORTS, rollbackOn = DAOException.class)
    public void deleteByParameterId(String parameterId) throws DAOException {
        try {
            CriteriaDelete<ApplicationParameterData> cq = deleteQuery();
            Root<ApplicationParameterData> root = cq.from(ApplicationParameterData.class);
            cq.where(
                    getEntityManager().getCriteriaBuilder()
                            .equal(root.get(ApplicationParameterData_.APPLICATION_PARAMETER_GUID), parameterId));
            getEntityManager().createQuery(cq).executeUpdate();
            getEntityManager().flush();
        } catch (Exception e) {
            throw handleConstraint(e, Errors.FAILED_TO_DELETE_BY_GUID_QUERY);
        }
    }

    enum Errors {
        FAILED_TO_DELETE_BY_GUID_QUERY;

    }
}
