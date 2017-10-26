package io.github.valfadeev.rundeck.plugin.nomad.common;

import java.util.Map;

public interface TaskConfigProvider {
    Map<String, Object> getConfig(Map<String, Object> config);
}
