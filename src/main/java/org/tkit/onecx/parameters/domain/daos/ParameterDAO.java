package org.tkit.onecx.parameters.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;
import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.createSearchStringPredicate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.criteria.NamesSearchCriteria;
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
                    .collect(Collectors.toMap(Parameter::getName,
                            p -> p.getValue() != null ? p.getValue() : p.getImportValue()));

        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED, e);
        }
    }

    public Stream<Parameter> findAllByProductNames(Set<String> productNames) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Parameter> cq = cb.createQuery(Parameter.class);
            Root<Parameter> root = cq.from(Parameter.class);
            if (productNames != null && !productNames.isEmpty()) {
                cq.where(root.get(Parameter_.PRODUCT_NAME).in(productNames));
            }

            return getEntityManager()
                    .createQuery(cq)
                    .getResultStream();
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_BY_PRODUCT_NAMES_FAILED, e);
        }
    }

    public Parameter findByNameApplicationIdAndProductName(String name, String applicationId, String productName) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Parameter> cq = cb.createQuery(Parameter.class);
            Root<Parameter> root = cq.from(Parameter.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(Parameter_.PRODUCT_NAME), productName));
            predicates.add(cb.equal(root.get(Parameter_.APPLICATION_ID), applicationId));
            predicates.add(cb.equal(root.get(Parameter_.NAME), name));
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
            return getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_BY_NAME_PRODUCT_NAME_APPLICATION_ID_FAILED, exception);
        }
    }

    @Transactional
    public PageResult<ParameterSearchResultItemTuple> searchByCriteria(ParameterSearchCriteria criteria) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Parameter> cq = cb.createQuery(Parameter.class);
            Root<Parameter> root = cq.from(Parameter.class);

            List<Predicate> predicates = new ArrayList<>();
            addSearchStringPredicate(predicates, cb, root.get(Parameter_.PRODUCT_NAME), criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(Parameter_.APPLICATION_ID), criteria.getApplicationId());

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                var namePredicate = createSearchStringPredicate(cb, root.get(Parameter_.NAME), criteria.getName());
                var displayNamePredicate = createSearchStringPredicate(cb, root.get(Parameter_.DISPLAY_NAME),
                        criteria.getName());
                predicates.add(cb.or(namePredicate, displayNamePredicate));
            }

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            PageResult<Parameter> parameterPageResult = createPageQuery(cq,
                    Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();

            // Map to ParameterSearchResultItemTuple and set the isInHistory flag
            List<ParameterSearchResultItemTuple> parameterTupleList = parameterPageResult.getStream()
                    .map(parameter -> {
                        CriteriaQuery<Long> subquery = cb.createQuery(Long.class);
                        Root<History> historyRoot = subquery.from(History.class);
                        subquery.select(cb.count(historyRoot));
                        subquery.where(
                                cb.equal(historyRoot.get(History_.NAME), parameter.getName()),
                                cb.equal(historyRoot.get(History_.APPLICATION_ID), parameter.getApplicationId()),
                                cb.equal(historyRoot.get(History_.PRODUCT_NAME), parameter.getProductName()),
                                cb.equal(historyRoot.get(History_.TENANT_ID), parameter.getTenantId()));
                        Long count = getEntityManager().createQuery(subquery).getSingleResult();
                        boolean isInHistory = count > 0;
                        return new ParameterSearchResultItemTuple(parameter, isInHistory);
                    })
                    .toList();

            return new PageResult<>(parameterPageResult.getTotalElements(), parameterTupleList.stream(),
                    parameterPageResult.getNumber(), parameterPageResult.getSize());
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_PARAMETERS_FAILED, exception);
        }
    }

    public PageResult<String> searchAllNames(NamesSearchCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Parameter> root = cq.from(Parameter.class);
            cq.select(root.get(Parameter_.NAME)).distinct(true);
            cq.where(cb.equal(root.get(Parameter_.PRODUCT_NAME), criteria.getProductName()));

            if (criteria.getApplicationId() != null && !criteria.getApplicationId().isEmpty()) {
                cq.where(cb.equal(root.get(Parameter_.APPLICATION_ID), criteria.getApplicationId()));
            }

            var results = getEntityManager().createQuery(cq).getResultList();
            return new PageResult<>(results.size(), results.stream(), Page.of(0, 1));
        } catch (Exception exception) {
            throw new DAOException(ErrorKeys.FIND_ALL_NAMES_FAILED, exception);
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

    public enum ErrorKeys {

        FIND_ALL_PARAMETERS_BY_APPLICATION_ID_FAILED,
        FIND_ALL_APPLICATIONS_FAILED,

        FIND_ALL_NAMES_FAILED,
        FIND_ALL_PARAMETERS_FAILED,
        FIND_BY_NAME_PRODUCT_NAME_APPLICATION_ID_FAILED,
        FIND_ALL_PARAMETERS_BY_PRODUCT_NAMES_FAILED
    }
}
