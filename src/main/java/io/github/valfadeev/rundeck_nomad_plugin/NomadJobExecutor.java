package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Evaluation;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.*;

import java.io.IOException;
import java.util.List;

import static com.hashicorp.nomad.javasdk.NomadPredicates.responseValue;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.allAllocationsFinished;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.either;
import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadAllocationPredicates.failedAllocationsOver;

public class NomadJobExecutor {

    private static final int LOG_LEVEL_ERROR = 0;
    private static final int LOG_LEVEL_INFO = 2;

    private final PluginLogger logger;
    private final JobsApi jobsApi;
    private final EvaluationsApi evaluationsApi;
    private final long maxFailurePercentage;

    //TODO: Wrap nomadUrl, maxFailurePercentage
    public NomadJobExecutor(PluginLogger logger, NomadApiClient client, long maxFailurePercentage) {
        this.logger = logger;
        this.jobsApi = client.getJobsApi();
        this.evaluationsApi = client.getEvaluationsApi();
        this.maxFailurePercentage = maxFailurePercentage;
    }

    public void execute(Job job) throws StepException {
        info("Registering job %s with Nomad", job.getId());
        String evaluationId = registerJob(job);
        info("Waiting for evaluation %s to complete...", evaluationId);
        waitForEvaluation(evaluationId);
        info("Evaluation %s is complete, waiting for allocations", evaluationId);
        ensureAllocationHealth(evaluationId);
        info("Job %s completed", job.getId());
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
            evaluation
                    .getFailedTgAllocs()
                    .forEach((taskGroup, allocation) -> allocation
                        .getDimensionExhausted()
                        .forEach((dimension, v) -> {
                            error("Evaluation blocked due to %s", dimension);
                        }));
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

    private void info(String format, Object... args) {
        logger.log(LOG_LEVEL_INFO, String.format(format, args));
    }

    private void error(String format, Object... args) {
        logger.log(LOG_LEVEL_ERROR, String.format(format, args));

    }
}
