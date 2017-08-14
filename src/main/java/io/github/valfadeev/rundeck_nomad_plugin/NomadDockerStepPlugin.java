package io.github.valfadeev.rundeck_nomad_plugin;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;

import io.github.valfadeev.rundeck_nomad_plugin.common.Driver;

import static io.github.valfadeev.rundeck_nomad_plugin.common.SupportedDrivers.DOCKER;


@Driver(name = DOCKER)
@Plugin(name = NomadDockerStepPlugin.SERVICE_PROVIDER_NAME,
        service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Run Docker container on Nomad",
                   description = "Submits a Nomad job running a Docker container")
public class NomadDockerStepPlugin extends NomadStepPlugin implements StepPlugin, Describable {

    public static final String SERVICE_PROVIDER_NAME
            = "io.github.valfadeev.rundeck_nomad_plugin.NomadDockerStepPlugin";
}
