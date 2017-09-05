package io.github.valfadeev.rundeck_nomad_plugin;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Evaluation;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.AgentApi;
import com.hashicorp.nomad.javasdk.EvaluationsApi;
import com.hashicorp.nomad.javasdk.JobsApi;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.QueryOptions;
import com.hashicorp.nomad.javasdk.WaitStrategy;
import io.github.valfadeev.rundeck_nomad_plugin.common.Driver;
import io.github.valfadeev.rundeck_nomad_plugin.common.PropertyComposer;
import io.github.valfadeev.rundeck_nomad_plugin.common.TaskConfigProvider;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadJobProvider;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadPropertyComposer;
import static com.hashicorp.nomad.javasdk.NomadPredicates.responseValue;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.allAllocationsFinished;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.either;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.failedAllocationsOver;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_MAX_FAIL_PCT;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_URL;


public abstract class NomadStepPlugin implements StepPlugin, Describable {

    private static final String JOB_TYPE_BATCH = "batch";
    private static final String TASK_GROUP_RUNDECK = "rundeck";

    private final String driverName = this.getClass().getAnnotation(Driver.class).name();
    private final String serviceProviderName = this.getClass().getAnnotation(Plugin.class).name();
    private final String title = this.getClass().getAnnotation(PluginDescription.class).title();
    private final String description = this.getClass().getAnnotation(PluginDescription.class).description();

    public Description getDescription() {
        try {
            PropertyComposer driverPropertyComposer =
                    (PropertyComposer) Class.forName(
                            String.format("%s.driver.%s.%sPropertyComposer",
                                    this.getClass().getPackage().getName(),
                                    driverName.toLowerCase(),
                                    driverName))
                            .newInstance();
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

    /**
     * This enum lists the known reasons this plugin might fail
     */
    static enum Reason implements FailureReason{
        AgentConfigReadFailure,
        AllocMaxFailExceeded,
        EvalBlockedFailure,
        EvalStatusPollFailure,
        JobRegistrationFailure,
        AllocStatusFailure,
        PluginInternalFailure
    }

    public void executeStep(final PluginStepContext context, final Map<String, Object> configuration) throws StepException {

        PluginLogger logger = context.getExecutionContext().getExecutionListener();

        // make job id and name unique for every run
        // https://github.com/hashicorp/nomad/issues/2149
        long ts = new Date().getTime();
        Map<String, String> rundeckJob = context.getDataContext().get("job");
        String rundeckJobId = String.format("%s-%s",rundeckJob.get("id"), ts);
        String rundeckJobName = String.format("%s-%s", rundeckJob.get("name"), ts);

        String nomadUrl = configuration
                .get(NOMAD_URL)
                .toString();
        NomadApiConfiguration config =
                new NomadApiConfiguration
                        .Builder()
                        .setAddress(nomadUrl)
                        .build();
        NomadApiClient apiClient = new NomadApiClient(config);

        // obtain current agent configuration to look up some default values
        Map<String, Object> agentConfig;
        AgentApi agentApi = apiClient.getAgentApi();
        try {
            agentConfig = agentApi
                    .self()
                    .getValue()
                    .getConfig();
        }
        catch (NomadException | IOException e) {
            throw new StepException("Error while getting agent configuration",
                    Reason.AgentConfigReadFailure);
        }

        TaskConfigProvider taskConfigProvider = null;
        try {
            taskConfigProvider =
                    (TaskConfigProvider) Class.forName(
                            String.format("%s.driver.%s.%sTaskConfigProvider",
                                    this.getClass().getPackage().getName(),
                                    driverName.toLowerCase(),
                                    driverName))
                    .getDeclaredConstructor(Map.class).newInstance(configuration);
        } catch (Exception e) {
            throw new StepException("Error while loading task configuration class",
                    Reason.PluginInternalFailure);
        }

        Job job = NomadJobProvider.getJob(
                configuration,
                agentConfig,
                taskConfigProvider.getConfig(configuration),
                driverName.toLowerCase(),
                rundeckJobId,
                rundeckJobName,
                TASK_GROUP_RUNDECK,
                JOB_TYPE_BATCH);

        JobsApi jobsApi = apiClient.getJobsApi();
        String evalId;
        logger.log(2, String.format("Registering job %s with Nomad", rundeckJobId));

        try {
            evalId = jobsApi.register(job).getValue();
        } catch (IOException|NomadException e) {
            throw new StepException(
                    String.format("Error while registering job %s with Nomad", rundeckJobId),
                    Reason.JobRegistrationFailure);
        }

        EvaluationsApi evaluationsApi = apiClient.getEvaluationsApi();
        Evaluation eval;
        logger.log(2, String.format("Waiting for evauation %s to complete...", evalId));
        try {
            eval = evaluationsApi
                    .pollForCompletion(evalId, WaitStrategy.WAIT_INDEFINITELY) // timeout should be set in Rundeck
                    .getValue();
        } catch (NomadException|IOException e) {
            throw new StepException(
                    String.format("Error while polling for evaluation status: %s", evalId),
                    Reason.EvalStatusPollFailure);
        }
        logger.log(2, String.format("Evauation %s is complete, waiting for allocations", evalId));

        if (!eval.getBlockedEval().isEmpty()) {
            eval.getFailedTgAllocs()
                    .get(TASK_GROUP_RUNDECK)
                    .getDimensionExhausted()
                    .keySet()
                    .forEach(
                    k -> logger.log(0,
                            String.format("Evaluation blocked due to %s", k)));
            throw new StepException(
                    String.format("Error while processing evaluation: %s", evalId),
                    Reason.EvalBlockedFailure);
        }

        // poll for allocation status; bail out if
        // the number of failed allocations exceeds
        // the threshold
        Long maxFailPct = Long.parseLong(
                configuration
                        .get(NOMAD_MAX_FAIL_PCT)
                        .toString());

        List<AllocationListStub> allocs;
        try {
            allocs = evaluationsApi
                    .allocations(evalId,
                            QueryOptions
                                    .pollRepeatedlyUntil(responseValue(
                                    either(allAllocationsFinished(),
                                           failedAllocationsOver(maxFailPct))),
                            WaitStrategy.WAIT_INDEFINITELY)) // timeout should be set in Rundeck
                    .getValue();
        } catch (IOException|NomadException e) {
            throw new StepException(
                    "Error while polling for allocation status",
                    Reason.AllocStatusFailure);
        }

        allocs.forEach(a -> logger.log(2,
                String.format("allocation %s on node %s: %s",
                        a.getId(),
                        a.getNodeId(),
                        a.getClientStatus())));

        if (failedAllocationsOver(maxFailPct).apply(allocs)) {
            throw new StepException("Too many allocations failed", Reason.AllocMaxFailExceeded);
        } else {
            logger.log(2, String.format("Job %s completed", rundeckJobName));
        }
    }
}
