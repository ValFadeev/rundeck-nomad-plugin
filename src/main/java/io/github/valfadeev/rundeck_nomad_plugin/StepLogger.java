package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.plugins.PluginLogger;

public class StepLogger {

    //TODO: Can this implement a standard logging api that also has a noop implementation
    private static final int LOG_LEVEL_ERROR = 0;
    private static final int LOG_LEVEL_INFO = 2;

    private final PluginLogger pluginLogger;

    public StepLogger(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;

    }

    public void info(String format, Object... args) {
        pluginLogger.log(LOG_LEVEL_INFO, String.format(format, args));
    }

    public void error(String format, Object... args) {
        pluginLogger.log(LOG_LEVEL_ERROR, String.format(format, args));
    }
}
