package io.github.valfadeev.rundeck.plugin.nomad.nomad;

import java.util.Map;

import com.hashicorp.nomad.apimodel.UpdateStrategy;

public class NomadUpdateStrategyProvider {

        public static UpdateStrategy getUpdate(Map<String, Object> configuration) {

            UpdateStrategy update = new UpdateStrategy();

            String maxParallel = configuration
                    .get(NomadConfigOptions.NOMAD_MAX_PARALLEL)
                    .toString();
            if (!maxParallel.isEmpty()) {
                update.setMaxParallel(Integer.parseInt(maxParallel));
            }

            String healthCheck = configuration
                    .get(NomadConfigOptions.NOMAD_HEALTH_CHECK)
                    .toString();
            if (!maxParallel.isEmpty()) {
                update.setHealthCheck(healthCheck);
            }

            String minHealthyTime = configuration
                    .get(NomadConfigOptions.NOMAD_MIN_HEALTHY_TIME)
                    .toString();
            if (!minHealthyTime.isEmpty()) {
                update.setMinHealthyTime(Long.parseLong(minHealthyTime));
            }

            String healthyDeadline = configuration
                    .get(NomadConfigOptions.NOMAD_HEALTHY_DEADLINE)
                    .toString();
            if (!healthyDeadline.isEmpty()) {
                update.setHealthyDeadline(Long.parseLong(healthyDeadline));
            }

            String autoRevert = configuration
                    .get(NomadConfigOptions.NOMAD_AUTO_REVERT)
                    .toString();
            if (!autoRevert.isEmpty()) {
                update.setAutoRevert(Boolean.parseBoolean(autoRevert));
            }

            String canary = configuration
                    .get(NomadConfigOptions.NOMAD_CANARY)
                    .toString();
            if (!canary.isEmpty()) {
                update.setCanary(Integer.parseInt(canary));
            }

            String stagger = configuration
                    .get(NomadConfigOptions.NOMAD_STAGGER)
                    .toString();
            if (!stagger.isEmpty()) {
                update.setStagger(Long.parseLong(canary));
            }

            return update;
        }
    }


