package org.tkit.onecx.parameters.domain.timer;

import java.time.LocalDateTime;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.tkit.onecx.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.onecx.parameters.domain.daos.JobDAO;
import org.tkit.onecx.parameters.domain.models.Job;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.runtime.configuration.DurationConverter;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MaintenanceHistoryService {

    @Inject
    ParameterConfig parameterConfig;

    @Inject
    ApplicationParameterHistoryDAO dao;

    @Inject
    JobDAO jobDAO;

    static final String JOB_ID = "maintenance.history";

    // find older items and delete it
    @Scheduled(identity = "maintenance.history", cron = "${onecx.parameter.scheduler.expression}")
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    void maintenanceHistoryData() {
        DurationConverter converter = new DurationConverter();
        LocalDateTime dt = LocalDateTime.now()
                .minus(converter.convert(parameterConfig.maintenanceHistoryScheduler().duration()));
        Job job = jobDAO.getJob(JOB_ID);
        if (job != null) {
            log.info("Scheduler for job id: '{}' started.", JOB_ID);
            try {
                dao.deleteApplicationHistoryOlderThan(dt);
                log.info("Scheduler for job id: '{}' finished.", JOB_ID);
            } catch (Exception ex) {
                log.error("Scheduler for job id: '" + JOB_ID + "' failed.", ex);
                throw ex;
            }
        }
    }
}
