package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import io.github.valfadeev.rundeck_nomad_plugin.common.Driver;
import io.github.valfadeev.rundeck_nomad_plugin.common.PropertyComposer;
import io.github.valfadeev.rundeck_nomad_plugin.common.TaskConfigProvider;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadPropertyComposer;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_MAX_FAIL_PCT;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_URL;


import java.util.Map;

public abstract class NomadStepPlugin implements StepPlugin, Describable {

    private static final String RUNDECK_JOB_CONTEXT_KEY = "job";
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
        // Build nomad job execution context
        PluginLogger pluginLogger = context.getExecutionContext().getExecutionListener();
        StepLogger logger = new StepLogger(pluginLogger);

        String nomadUrl = configuration.get(NOMAD_URL).toString();
        long maxFailurePercentage = getMaximumFailurePercentage(configuration);
        NomadApiClient apiClient = buildNomadApiClient(nomadUrl);
        JobLauncher launcher = new JobLauncher(logger, apiClient, maxFailurePercentage);

        Map<String, String> rundeckJob = context.getDataContext().get(RUNDECK_JOB_CONTEXT_KEY);
        Map<String, Object> taskConfig = buildTaskConfig(configuration);

        NomadRundeckJobBuilder builder = new NomadRundeckJobBuilder(driverName, apiClient, configuration);

        //Execute
        logger.info("Creating job");
        Job job = builder.createJob(rundeckJob, taskConfig);
        logger.info("Registering job %s with Nomad", job.getId());
        String evaluationId = launcher.registerJob(job);
        logger.info("Waiting for evaluation %s to complete...", evaluationId);
        launcher.waitForEvaluation(evaluationId);
        logger.info("Evaluation %s is complete, waiting for allocations", evaluationId);
        launcher.ensureAllocationHealth(evaluationId);
        logger.info("Job %s completed", job.getId());
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

    private NomadApiClient buildNomadApiClient(String nomadUrl) {
        NomadApiConfiguration config =
                new NomadApiConfiguration
                        .Builder()
                        .setAddress(nomadUrl)
                        .build();
        return new NomadApiClient(config);
    }

    private long getMaximumFailurePercentage(Map<String, Object> configuration) {
        return Long.parseLong(configuration
                .get(NOMAD_MAX_FAIL_PCT)
                .toString());
    }
}
