package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Evaluation;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.*;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadJobProvider;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.hashicorp.nomad.javasdk.NomadPredicates.responseValue;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.allAllocationsFinished;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.either;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.failedAllocationsOver;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_MAX_FAIL_PCT;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_URL;

public class NomadStepExecutor {

    private static final int LOG_LEVEL_INFO = 2;

    private static final String JOB_TYPE_BATCH = "batch";
    private static final String TASK_GROUP_RUNDECK = "rundeck";

    private final PluginLogger logger;
    private final Map<String, String> rundeckJob;
    private final Map<String, Object> stepConfiguration;
    private final NomadApiClient apiClient;
    private final JobsApi jobsApi;
    private final EvaluationsApi evaluationsApi;
    private final long maxFailurePercentage;

    public NomadStepExecutor(PluginLogger logger,
                             Map<String, String> rundeckJob,
                             Map<String, Object> stepConfiguration
    ) {
        this.logger = logger;
        this.rundeckJob = rundeckJob;
        this.stepConfiguration = stepConfiguration;
        this.apiClient = buildNomadApiClient();
        this.jobsApi = apiClient.getJobsApi();
        this.evaluationsApi = apiClient.getEvaluationsApi();
        this.maxFailurePercentage = getMaxFailurePercentage();
    }

    public void execute(String driverName, Map<String, Object> taskConfig) throws StepException {
        info("Creating new job");
        Job job = createNewJob(driverName, taskConfig);
        info("Registering job %s with Nomad", job.getId());
        String evaluationId = registerJob(job);
        info("Waiting for evaluation %s to complete...", evaluationId);
        waitForEvaluation(evaluationId);
        info("Evaluation %s is complete, waiting for allocations", evaluationId);
        ensureAllocationHealth(evaluationId);
        info("Job %s completed", job.getId());
    }

    private Job createNewJob(String driverName, Map<String, Object> taskConfig) throws StepException {
        long ts = new Date().getTime();
        // make job id and name unique for every run
        // https://github.com/hashicorp/nomad/issues/2149
        String jobId = createTimeStampedJobId(ts);
        String jobName = createTimestampedJobName(ts);

        try {
            Map<String, Object> agentConfig = buildAgentConfig();

            return NomadJobProvider.getJob(
                    stepConfiguration,
                    agentConfig,
                    taskConfig,
                    driverName,
                    jobId,
                    jobName,
                    TASK_GROUP_RUNDECK,
                    JOB_TYPE_BATCH);
        } catch (NomadException | IOException e) {
            throw new StepException("Error while getting agent configuration",
                    NomadStepFailure.AgentConfigReadFailure);
        }
    }

    private String registerJob(Job job) throws StepException {
        try {
            return jobsApi.register(job).getValue();
        } catch (IOException | NomadException e) {
            throw new StepException(
                    String.format("Error while registering job %s with Nomad", job.getId()),
                    NomadStepFailure.JobRegistrationFailure);
        }
    }

    private void waitForEvaluation(String evaluationId) throws StepException {
        Evaluation evaluation;
        try {
            // timeout should be set in Rundeck
            evaluation = evaluationsApi
                    .pollForCompletion(evaluationId, WaitStrategy.WAIT_INDEFINITELY)
                    .getValue();
        } catch (NomadException|IOException e) {
            throw new StepException(
                    String.format("Error while polling for evaluation status: %s", evaluationId),
                    NomadStepFailure.EvalStatusPollFailure);
        }
        if (!evaluation.getBlockedEval().isEmpty()) {
            evaluation.getFailedTgAllocs()
                    .get(TASK_GROUP_RUNDECK)
                    .getDimensionExhausted()
                    .forEach(
                    (k, v) -> logger.log(0,
                            String.format("Evaluation blocked due to %s", k)));
            throw new StepException(
                    String.format("Error while processing evaluation: %s", evaluation),
                    NomadStepFailure.EvalBlockedFailure);
        }
    }

    private void ensureAllocationHealth(String evaluationId) throws StepException {
        List<AllocationListStub> allocations = fetchAllocations(evaluationId);

        allocations.forEach(a -> info("allocation %s on node %s: %s",
                        a.getId(),
                        a.getNodeId(),
                        a.getClientStatus()));

        if (failedAllocationsOver(maxFailurePercentage).apply(allocations)) {
            throw new StepException("Too many allocations failed", NomadStepFailure.AllocMaxFailExceeded);
        }
    }

    private List<AllocationListStub> fetchAllocations(String evaluationId) throws StepException {
        try {
            // timeout should be set in Rundeck
            return evaluationsApi.allocations(evaluationId,
                        QueryOptions.pollRepeatedlyUntil(responseValue(either(
                                allAllocationsFinished(),
                                failedAllocationsOver(maxFailurePercentage))),
                                WaitStrategy.WAIT_INDEFINITELY))
                        .getValue();
        } catch (IOException | NomadException e) {
            throw new StepException(
                    "Error while polling for allocation status",
                    NomadStepFailure.AllocStatusFailure);
        }
    }

    private NomadApiClient buildNomadApiClient() {
        String nomadUrl = stepConfiguration.get(NOMAD_URL).toString();
        NomadApiConfiguration config =
                new NomadApiConfiguration
                        .Builder()
                        .setAddress(nomadUrl)
                        .build();
        return new NomadApiClient(config);
    }

    private Map<String, Object> buildAgentConfig() throws IOException, NomadException {
        AgentApi agentApi = apiClient.getAgentApi();
        return agentApi.self()
                .getValue()
                .getConfig();
    }

    private Long getMaxFailurePercentage() {
        return Long.parseLong(stepConfiguration
                        .get(NOMAD_MAX_FAIL_PCT)
                        .toString());
    }

    private String createTimeStampedJobId(long ts) {
        return String.format("%s-%s",rundeckJob.get("id"), ts);
    }

    private String createTimestampedJobName(long ts) {
        return String.format("%s-%s", rundeckJob.get("name"), ts);
    }

    private void info(String format, Object... args) {
        logger.log(LOG_LEVEL_INFO, String.format(format, args));

    }
}
