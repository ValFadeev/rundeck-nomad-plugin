package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.AgentApi;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadException;
import io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadJobProvider;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class NomadRundeckJobBuilder {

    private static final String JOB_TYPE_BATCH = "batch";
    private static final String TASK_GROUP_RUNDECK = "rundeck";

    private final String driverName;
    private final NomadApiClient apiClient;
    private final Map<String, Object> stepConfiguration;

    public NomadRundeckJobBuilder(String driverName, NomadApiClient agentApi, Map<String, Object> stepConfiguration) {
        this.driverName = driverName;
        this.apiClient = agentApi;
        this.stepConfiguration = stepConfiguration;
    }

    public Job createJob(Map<String, String> rundeckJob, Map<String, Object> taskConfig) throws StepException {
            long ts = new Date().getTime();
            // make job id and name unique for every run
            // https://github.com/hashicorp/nomad/issues/2149
            String jobId = String.format("%s-%s",rundeckJob.get("id"), ts);
            String jobName = String.format("%s-%s", rundeckJob.get("name"), ts);

            try {
                Map<String, Object> agentConfig = getAgentConfig();

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


    private Map<String,Object> getAgentConfig() throws IOException, NomadException {
        AgentApi agentApi = apiClient.getAgentApi();
        return agentApi
                .self()
                .getValue()
                .getConfig();
    }
}
