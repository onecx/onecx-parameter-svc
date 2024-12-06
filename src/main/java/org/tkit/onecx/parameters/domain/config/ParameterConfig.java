package org.tkit.onecx.parameters.domain.config;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Parameter svc configuration
 */
@ConfigDocFilename("onecx-parameter-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.parameter")
public interface ParameterConfig {

    /**
     * Maintenance history scheduler configurations
     */
    @WithName("scheduler")
    MaintenanceHistoryScheduler maintenanceHistoryScheduler();

    interface MaintenanceHistoryScheduler {
        /**
         * Scheduler duration in days
         */
        @WithDefault("7")
        @WithName("duration")
        String duration();

        /**
         * Scheduler expression
         */
        @WithDefault("0 15 2 * * ?")
        @WithName("expression")
        String expression();
    }
}
