package io.github.valfadeev.rundeck_nomad_plugin.common;

import java.util.Map;

public interface TaskConfigProvider {
    Map<String, Object> getConfig(Map<String, Object> config);
}
