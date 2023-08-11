package org.tkit.parameters.domain.timer;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tkit.parameters.domain.daos.ApplicationParameterHistoryDAO;
import org.tkit.parameters.domain.daos.JobDAO;
import org.tkit.parameters.domain.di.models.Job;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MaintenanceHistoryService {

    @ConfigProperty(name = "maintenance.history.scheduler.duration", defaultValue = "P7D")
    Duration duration;

    @Inject
    ApplicationParameterHistoryDAO dao;

    @Inject
    JobDAO jobDAO;

    static final String JOB_ID = "maintenance.history";

    // find older items and delete it
    @Scheduled(identity = "maintenance.history", cron = "${maintenance.history.scheduler.expr}")
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    void maintenanceHistoryData() {
        LocalDateTime dt = LocalDateTime.now().minus(duration);
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
