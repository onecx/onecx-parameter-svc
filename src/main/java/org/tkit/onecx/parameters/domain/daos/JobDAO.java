package org.tkit.onecx.parameters.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.models.Job;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class JobDAO extends AbstractDAO<Job> {

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public Job getJob(String id) throws DAOException {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Job> cq = cb.createQuery(Job.class);
            Root<Job> root = cq.from(Job.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));

            return getEntityManager()
                    .createQuery(cq)
                    .setMaxResults(1)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .setHint("jakarta.persistence.lock.timeout", -2)
                    .getSingleResult();

        } catch (NoResultException ex) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(Error.GET_JOB_FAILED, ex);
        }
    }

    public enum Error {

        GET_JOB_FAILED;
    }
}
