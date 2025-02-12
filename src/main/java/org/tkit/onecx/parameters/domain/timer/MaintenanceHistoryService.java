package org.tkit.onecx.parameters.domain.timer;

import java.time.LocalDateTime;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.config.ParameterConfig;
import org.tkit.onecx.parameters.domain.daos.HistoryDAO;
import org.tkit.onecx.parameters.domain.daos.JobDAO;
import org.tkit.onecx.parameters.domain.models.Job;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.tenant.ContextTenantResolverConfig;

import io.quarkus.runtime.configuration.DurationConverter;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MaintenanceHistoryService {

    @Inject
    ParameterConfig parameterConfig;

    @Inject
    HistoryDAO dao;

    @Inject
    JobDAO jobDAO;

    @Inject
    ContextTenantResolverConfig tenantConfig;

    static final String JOB_ID = "maintenance.history";
    private static final String PRINCIPAL = "apm-principal-token";

    // find older items and delete it
    @Scheduled(identity = "maintenance.history", cron = "${onecx.parameter.scheduler.expression}")
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    void maintenanceHistoryData() {
        DurationConverter converter = new DurationConverter();
        LocalDateTime dt = LocalDateTime.now()
                .minus(converter.convert(parameterConfig.maintenanceHistoryScheduler().duration()));
        try {
            log.info("Scheduler for job id: '{}' started.", JOB_ID);
            var tenantId = Boolean.TRUE.equals(tenantConfig.root().enabled()) ? tenantConfig.root().value()
                    : tenantConfig.defaultTenantValue();
            var ctx = Context.builder()
                    .principal(PRINCIPAL)
                    .tenantId(tenantId)
                    .build();

            ApplicationContext.start(ctx);
            Job job = jobDAO.getJob(JOB_ID);
            if (job != null) {
                dao.deleteApplicationHistoryOlderThan(dt);
                log.info("Scheduler for job id: '{}' finished.", JOB_ID);
            }
        } catch (Exception ex) {
            log.error("Scheduler for job id: '" + JOB_ID + "' failed.", ex);
            throw ex;
        }
    }
}