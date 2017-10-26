package io.github.valfadeev.rundeck.plugin.nomad;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import io.github.valfadeev.rundeck.plugin.nomad.common.Driver;
import io.github.valfadeev.rundeck.plugin.nomad.common.SupportedDrivers;

@Driver(name = SupportedDrivers.JAVA)
@Plugin(name = NomadJavaStepPlugin.SERVICE_PROVIDER_NAME,
        service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Run java application on Nomad",
                   description = "Submits a Nomad job running a java application packaged into a Jar file.")
public class NomadJavaStepPlugin extends NomadStepPlugin implements StepPlugin, Describable {

    public static final String SERVICE_PROVIDER_NAME
            = "io.github.valfadeev.rundeck.plugin.nomad.NomadJavaStepPlugin";
}
