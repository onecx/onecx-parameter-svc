package org.tkit.onecx.parameters.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.criteria.HistorySearchCriteria;
import org.tkit.onecx.parameters.domain.models.*;
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

            addSearchStringPredicate(predicates, cb, root.get(History_.PRODUCT_NAME), criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(History_.APPLICATION_ID), criteria.getApplicationId());
            addSearchStringPredicate(predicates, cb, root.get(History_.NAME), criteria.getName());

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
            addSearchStringPredicate(predicates, cb, root.get(History_.PRODUCT_NAME), criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(History_.APPLICATION_ID), criteria.getApplicationId());
            addSearchStringPredicate(predicates, cb, root.get(History_.NAME), criteria.getName());

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

            addSearchStringPredicate(predicates, cb, root.get(History_.PRODUCT_NAME), criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(History_.APPLICATION_ID), criteria.getApplicationId());
            addSearchStringPredicate(predicates, cb, root.get(History_.NAME), criteria.getName());

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            return em.createQuery(cq).getResultList();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_HISTORY_FAILED, exception);
        }
    }

    public List<ApplicationTuple> searchAllProductNamesAndApplicationIds() {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<ApplicationTuple> cq = cb.createQuery(ApplicationTuple.class);
            Root<History> root = cq.from(History.class);
            cq.select(
                    cb.construct(ApplicationTuple.class, root.get(Parameter_.PRODUCT_NAME),
                            root.get(Parameter_.APPLICATION_ID)))
                    .distinct(true);
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_APPLICATIONS_FAILED, exception);
        }
    }

    public enum ErrorKeys {
        DELETE_PARAMETER_HISTORY_OLDER_THAN_FAILED,
        FIND_ALL_APPLICATIONS_FAILED,
        FIND_ALL_PARAMETERS_HISTORY_FAILED;
    }
}
