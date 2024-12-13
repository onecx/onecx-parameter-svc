package org.tkit.onecx.parameters.domain.daos;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.criteria.KeysSearchCriteria;
import org.tkit.onecx.parameters.domain.criteria.ParameterSearchCriteria;
import org.tkit.onecx.parameters.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class ParameterDAO extends AbstractDAO<Parameter> {

    public Map<String, String> findAllByProductNameAndApplicationId(String productName, String applicationId) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Parameter> cq = cb.createQuery(Parameter.class);
            Root<Parameter> root = cq.from(Parameter.class);
            cq.where(cb.and(
                    cb.equal(root.get(Parameter_.PRODUCT_NAME), productName),
                    cb.equal(root.get(Parameter_.APPLICATION_ID), applicationId),
                    cb.or(
                            cb.isNotNull(root.get(Parameter_.VALUE)),
                            cb.isNotNull(root.get(Parameter_.IMPORT_VALUE)))));

            return getEntityManager()
                    .createQuery(cq)
                    .getResultStream()
                    .collect(Collectors.toMap(Parameter::getKey,
                            p -> p.getValue() != null ? p.getValue() : p.getImportValue()));

        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED, e);
        }
    }

    public PageResult<Parameter> searchByCriteria(ParameterSearchCriteria criteria) {
        try {
            CriteriaQuery<Parameter> cq = criteriaQuery();
            Root<Parameter> root = cq.from(Parameter.class);
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
            if (criteria.getKey() != null && !criteria.getKey().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.KEY)), stringPattern(criteria.getKey())));
            }
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get(Parameter_.NAME)), stringPattern(criteria.getName())));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_FAILED, exception);
        }
    }

    public PageResult<String> searchAllKeys(KeysSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Parameter> root = cq.from(Parameter.class);
            cq.select(root.get(Parameter_.KEY)).distinct(true);

            if (criteria.getProductName() != null && !criteria.getProductName().isEmpty()) {
                cq.where(cb.equal(root.get(Parameter_.PRODUCT_NAME), criteria.getProductName()));
            }

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                cq.where(cb.equal(root.get(Parameter_.APPLICATION_ID), criteria.getApplicationId()));
            }

            var results = getEntityManager().createQuery(cq).getResultList();
            return new PageResult<>(results.size(), results.stream(), Page.of(0, 1));
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_KEYS_FAILED, exception);
        }
    }

    public List<ApplicationTuple> searchAllProductNamesAndApplicationIds() {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<ApplicationTuple> cq = cb.createQuery(ApplicationTuple.class);
            Root<Parameter> root = cq.from(Parameter.class);
            cq.select(
                    cb.construct(ApplicationTuple.class, root.get(Parameter_.PRODUCT_NAME),
                            root.get(Parameter_.APPLICATION_ID)))
                    .distinct(true);
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_APPLICATIONS_FAILED, exception);
        }
    }

    private static String stringPattern(String value) {
        return (value.toLowerCase() + "%");
    }

    public enum ErrorKeys {

        FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED,
        FIND_ALL_APPLICATIONS_FAILED,

        FIND_ALL_KEYS_FAILED,
        FIND_ALL_PARAMETERS_FAILED;
    }
}
