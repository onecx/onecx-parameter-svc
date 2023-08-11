package org.tkit.parameters.domain.daos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import org.tkit.parameters.domain.criteria.ApplicationParameterHistorySearchCriteria;
import org.tkit.parameters.domain.models.ApplicationParameterHistory;
import org.tkit.parameters.domain.models.ApplicationParameterHistory_;
import org.tkit.parameters.domain.models.ApplicationParameter_;
import org.tkit.parameters.domain.models.ParameterHistoryCountTuple;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class ApplicationParameterHistoryDAO extends AbstractDAO<ApplicationParameterHistory> {

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteApplicationHistoryOlderThan(LocalDateTime date) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaDelete<ApplicationParameterHistory> cd = deleteQuery();
            Root<ApplicationParameterHistory> root = cd.from(ApplicationParameterHistory.class);
            cd.where(cb.lessThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE), date));
            getEntityManager().createQuery(cd).executeUpdate();
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.DELETE_PARAMETER_HISTORY_OLDER_THAN_FAILED, e);
        }
    }

    public PageResult<ApplicationParameterHistory> searchByCriteria(ApplicationParameterHistorySearchCriteria criteria) {
        try {
            CriteriaQuery<ApplicationParameterHistory> cq = criteriaQuery();
            Root<ApplicationParameterHistory> root = cq.from(ApplicationParameterHistory.class);
            List<Predicate> predicates = new ArrayList<>();
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(ApplicationParameter_.APPLICATION_ID)),
                        stringPattern(criteria.getApplicationId())));
            }
            if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(ApplicationParameterHistory_.KEY)), stringPattern(criteria.getKey())));
            }
            if (!criteria.getType().isEmpty()) {
                predicates.add(cb.lower(root.get(ApplicationParameterHistory_.TYPE)).in(toLowerCase(criteria.getType())));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            cq.orderBy(cb.asc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED, exception);
        }
    }

    public PageResult<ApplicationParameterHistory> searchDistinctByCriteria(
            ApplicationParameterHistorySearchCriteria criteria) {

        String selectQuery = "SELECT DISTINCT ON (application_id) * FROM apm_app_param_history";
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");

        if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
            whereClause.append(" AND application_id LIKE :applicationId");
        }

        if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
            whereClause.append(" AND param_key LIKE :key");
        }

        Query query = getEntityManager().createNativeQuery(selectQuery + whereClause, ApplicationParameterHistory.class);

        if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
            query.setParameter("applicationId", criteria.getApplicationId() + "%");
        }

        if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
            query.setParameter("key", criteria.getKey() + "%");
        }

        Stream results = query.getResultStream();

        return new PageResult<>(1, query.getResultStream(), Page.of(0, 1));
    }

    public PageResult<ApplicationParameterHistory> searchOnlyLatestByCriteria(
            ApplicationParameterHistorySearchCriteria criteria) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<ApplicationParameterHistory> cq = cb.createQuery(ApplicationParameterHistory.class);
            Root<ApplicationParameterHistory> root = cq.from(ApplicationParameterHistory.class);

            Subquery<Number> maxDateSubquery = cq.subquery(Number.class);
            Root<ApplicationParameterHistory> maxDateSubqueryRoot = maxDateSubquery.from(ApplicationParameterHistory.class);
            maxDateSubquery.select(cb.max(maxDateSubqueryRoot.get(AbstractTraceableEntity_.CREATION_DATE)))
                    .groupBy(maxDateSubqueryRoot.get(ApplicationParameterHistory_.INSTANCE_ID));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get(AbstractTraceableEntity_.CREATION_DATE).in(maxDateSubquery));

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get(ApplicationParameter_.APPLICATION_ID)),
                        criteria.getApplicationId().toLowerCase()));
            }
            if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get(ApplicationParameterHistory_.KEY)), criteria.getKey().toLowerCase()));
            }

            cq.select(root)
                    .where(cb.and(predicates.toArray(new Predicate[0])))
                    .groupBy(root.get(ApplicationParameterHistory_.INSTANCE_ID), root.get(TraceableEntity_.ID));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED, exception);
        }
    }

    public List<ParameterHistoryCountTuple> searchCountsByCriteria(ApplicationParameterHistorySearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<ParameterHistoryCountTuple> cq = cb.createQuery(ParameterHistoryCountTuple.class);
            Root<ApplicationParameterHistory> root = cq.from(ApplicationParameterHistory.class);
            cq.select(
                    cb.construct(ParameterHistoryCountTuple.class, root.get(AbstractTraceableEntity_.CREATION_DATE),
                            root.get(ApplicationParameterHistory_.COUNT)));
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(ApplicationParameter_.APPLICATION_ID)),
                        stringPattern(criteria.getApplicationId())));
            }
            if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(ApplicationParameterHistory_.KEY)), stringPattern(criteria.getKey())));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            return em.createQuery(cq).getResultList();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED, exception);
        }
    }

    private static String stringPattern(String value) {
        return (value.toLowerCase() + "%");
    }

    private static List<String> toLowerCase(List<String> value) {
        return value.stream().map(String::toLowerCase).toList();
    }

    public enum ErrorKeys {
        DELETE_PARAMETER_HISTORY_OLDER_THAN_FAILED,
        FIND_ALL_PARAMETERS_HISTORY_FAILED;
    }
}
