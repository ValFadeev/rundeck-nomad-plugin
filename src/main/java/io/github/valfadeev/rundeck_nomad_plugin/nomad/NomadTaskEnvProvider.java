package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.HashMap;
import java.util.Map;

import io.github.valfadeev.rundeck_nomad_plugin.util.ParseInput;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;

public class NomadTaskEnvProvider {

    public static Map<String, String> getEnv(Map<String, Object> configuration) {
        String envVarString = configuration
                .get(NOMAD_ENV_VARS)
                .toString();
        Map<String, String> taskEnv = new HashMap<>();
        if (!envVarString.isEmpty()) {
            taskEnv = ParseInput.kvToMap(envVarString);
        }
        return taskEnv;
    }
}
