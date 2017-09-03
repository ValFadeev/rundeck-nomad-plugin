package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import io.github.valfadeev.rundeck_nomad_plugin.common.Driver;
import io.github.valfadeev.rundeck_nomad_plugin.common.PropertyComposer;
import io.github.valfadeev.rundeck_nomad_plugin.common.TaskConfigProvider;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadPropertyComposer;

import java.util.Map;

public abstract class NomadStepPlugin implements StepPlugin, Describable {

    private final String driverName = this.getClass().getAnnotation(Driver.class).name();
    private final String serviceProviderName = this.getClass().getAnnotation(Plugin.class).name();
    private final String title = this.getClass().getAnnotation(PluginDescription.class).title();
    private final String description = this.getClass().getAnnotation(PluginDescription.class).description();

    public Description getDescription() {
        try {
            PropertyComposer driverPropertyComposer = reflectDriverPropertyComposer();
            PropertyComposer nomadPropertyComposer = new NomadPropertyComposer();
            return driverPropertyComposer
                    .compose(nomadPropertyComposer)
                    .getBuilder()
                    .name(serviceProviderName)
                    .title(title)
                    .description(description)
                    .build();
        } catch (Exception e) {
            System.out.println(String.format("could not load property composer class: %s", e.getMessage()));
            return null;
        }
    }

    public void executeStep(final PluginStepContext context, final Map<String, Object> configuration) throws StepException {
        PluginLogger logger = context.getExecutionContext().getExecutionListener();
        Map<String, String> rundeckJob = context.getDataContext().get("job");
        Map<String, Object> taskConfig = buildTaskConfig(configuration);
        NomadStepExecutor executor = new NomadStepExecutor(logger, rundeckJob, configuration);
        executor.execute(driverName.toLowerCase(), taskConfig);
    }

    private Map<String, Object> buildTaskConfig(Map<String, Object> configuration) throws StepException {
        try {
            TaskConfigProvider taskConfigProvider = reflectTaskConfigProvider(configuration);
            return taskConfigProvider.getConfig();
        } catch (Exception e) {
            throw new StepException("Error while loading task configuration class",
                    NomadStepFailure.PluginInternalFailure);
        }
    }

    private TaskConfigProvider reflectTaskConfigProvider(Map<String, Object> configuration)
            throws ReflectiveOperationException {
        String className = String.format("%s.driver.%s.%sTaskConfigProvider",
                this.getClass().getPackage().getName(),
                driverName.toLowerCase(),
                driverName);
        return (TaskConfigProvider) Class.forName(className)
                .getDeclaredConstructor(Map.class)
                .newInstance(configuration);
    }

    private PropertyComposer reflectDriverPropertyComposer() throws ReflectiveOperationException {
        String className = String.format("%s.driver.%s.%sPropertyComposer",
                this.getClass().getPackage().getName(),
                driverName.toLowerCase(),
                driverName);
        return (PropertyComposer) Class.forName(className).newInstance();
    }
}
