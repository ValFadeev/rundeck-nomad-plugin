package io.github.valfadeev.rundeck.plugin.nomad.nomad;

import java.util.HashMap;
import java.util.Map;

import io.github.valfadeev.rundeck.plugin.nomad.util.ParseInput;

public class NomadTaskEnvProvider {

    public static Map<String, String> getEnv(Map<String, Object> configuration) {
        String envVarString = configuration
                .get(NomadConfigOptions.NOMAD_ENV_VARS)
                .toString();
        Map<String, String> taskEnv = new HashMap<>();
        if (!envVarString.isEmpty()) {
            taskEnv = ParseInput.kvToMap(envVarString);
        }
        return taskEnv;
    }
}
