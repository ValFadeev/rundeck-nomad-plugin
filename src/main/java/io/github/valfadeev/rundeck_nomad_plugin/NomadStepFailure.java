package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;

public enum NomadStepFailure implements FailureReason {
    AgentConfigReadFailure,
    AllocMaxFailExceeded,
    EvalBlockedFailure,
    EvalStatusPollFailure,
    JobRegistrationFailure,
    AllocStatusFailure,
    PluginInternalFailure
}
