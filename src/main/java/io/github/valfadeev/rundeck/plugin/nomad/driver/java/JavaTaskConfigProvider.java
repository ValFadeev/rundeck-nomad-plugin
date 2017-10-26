package io.github.valfadeev.rundeck.plugin.nomad.driver.java;

import java.util.HashMap;
import java.util.Map;

import io.github.valfadeev.rundeck.plugin.nomad.common.TaskConfigProvider;

public class JavaTaskConfigProvider implements TaskConfigProvider{

    private Map<String, Object> configuration;

    public JavaTaskConfigProvider(Map<String, Object> configuration) {this.configuration = configuration; }

    @Override
    public Map<String, Object> getConfig(Map<String, Object> config) {
        Map<String, Object> taskConfig = new HashMap<>();

        String javaClass = this.configuration.get(JavaConfigOptions.JAVA_CLASS).toString();
        if (!javaClass.isEmpty()) {
            taskConfig.put("class", javaClass);
        }

        String classPath = this.configuration.get(JavaConfigOptions.JAVA_CLASS_PATH).toString();
        if (!classPath.isEmpty()) {
            taskConfig.put("class_path", classPath);
        }

        String jarPath = this.configuration.get(JavaConfigOptions.JAVA_JAR_PATH).toString();
        if (!jarPath.isEmpty()) {
            taskConfig.put("jar_path", jarPath);
        }

        String javaArgsString = this.configuration.get(JavaConfigOptions.JAVA_ARGS).toString();
        if (!javaArgsString.isEmpty()) {
            String[] javaArgs = javaArgsString.split(",");
            taskConfig.put("args", javaArgs);
        }

        String jvmOptionsString = this.configuration.get(JavaConfigOptions.JAVA_JVM_OPTIONS).toString();
        if (!jvmOptionsString.isEmpty()) {
            String[] jvmOptions = jvmOptionsString.split(",");
            taskConfig.put("args", jvmOptions);
        }

        return taskConfig;
    }
}
