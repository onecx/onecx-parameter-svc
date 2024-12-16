package org.tkit.onecx.parameters.domain.daos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.criteria.HistorySearchCriteria;
import org.tkit.onecx.parameters.domain.models.History;
import org.tkit.onecx.parameters.domain.models.HistoryCountTuple;
import org.tkit.onecx.parameters.domain.models.History_;
import org.tkit.onecx.parameters.domain.models.Parameter_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class HistoryDAO extends AbstractDAO<History> {

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteApplicationHistoryOlderThan(LocalDateTime date) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaDelete<History> cd = deleteQuery();
            Root<History> root = cd.from(History.class);
            cd.where(cb.lessThanOrEqualTo(root.get(AbstractTraceableEntity_.CREATION_DATE), date));
            getEntityManager().createQuery(cd).executeUpdate();
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.DELETE_PARAMETER_HISTORY_OLDER_THAN_FAILED, e);
        }
    }

    public PageResult<History> searchByCriteria(HistorySearchCriteria criteria) {
        try {
            CriteriaQuery<History> cq = criteriaQuery();
            Root<History> root = cq.from(History.class);
            List<Predicate> predicates = new ArrayList<>();
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

            if (criteria.getProductName() != null && !criteria.getProductName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.PRODUCT_NAME)),
                        stringPattern(criteria.getProductName())));
            }

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.APPLICATION_ID)),
                        stringPattern(criteria.getApplicationId())));
            }
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(History_.NAME)), stringPattern(criteria.getName())));
            }
            if (!criteria.getType().isEmpty()) {
                predicates.add(cb.lower(root.get(History_.TYPE)).in(toLowerCase(criteria.getType())));
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

    public PageResult<History> searchOnlyLatestByCriteria(
            HistorySearchCriteria criteria) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<History> cq = cb.createQuery(History.class);
            Root<History> root = cq.from(History.class);

            Subquery<Number> maxDateSubquery = cq.subquery(Number.class);
            Root<History> maxDateSubqueryRoot = maxDateSubquery.from(History.class);
            maxDateSubquery.select(cb.max(maxDateSubqueryRoot.get(AbstractTraceableEntity_.CREATION_DATE)))
                    .groupBy(maxDateSubqueryRoot.get(History_.INSTANCE_ID));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get(AbstractTraceableEntity_.CREATION_DATE).in(maxDateSubquery));

            if (criteria.getProductName() != null && !criteria.getProductName().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get(Parameter_.PRODUCT_NAME)),
                        criteria.getProductName().toLowerCase()));
            }
            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get(Parameter_.APPLICATION_ID)),
                        criteria.getApplicationId().toLowerCase()));
            }
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get(History_.NAME)), criteria.getName().toLowerCase()));
            }

            cq.select(root)
                    .where(cb.and(predicates.toArray(new Predicate[0])))
                    .groupBy(root.get(History_.INSTANCE_ID), root.get(TraceableEntity_.ID));

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED, exception);
        }
    }

    public List<HistoryCountTuple> searchCountsByCriteria(HistorySearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<HistoryCountTuple> cq = cb.createQuery(HistoryCountTuple.class);
            Root<History> root = cq.from(History.class);
            cq.select(
                    cb.construct(HistoryCountTuple.class, root.get(AbstractTraceableEntity_.CREATION_DATE),
                            root.get(History_.COUNT)));
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getProductName() != null && !criteria.getProductName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.PRODUCT_NAME)),
                        stringPattern(criteria.getProductName())));
            }

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.APPLICATION_ID)),
                        stringPattern(criteria.getApplicationId())));
            }
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(History_.NAME)), stringPattern(criteria.getName())));
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
