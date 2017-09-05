package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import io.github.valfadeev.rundeck_nomad_plugin.common.PropertyComposer;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;

public class NomadPropertyComposer extends PropertyComposer {

    @Override
    public DescriptionBuilder addProperties(DescriptionBuilder builder) {
        return builder
                .property(PropertyBuilder.builder()
                        .string(NOMAD_URL)
                        .title("Nomad agent URL")
                        .description("URL of the Nomad agent to submit job (including url scheme and port)")
                        .required(true)
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_DATACENTER)
                        .title("Nomad datacenter")
                        .description("A list of datacenters in the region "
                                + "which are eligible for task placement. "
                                + "Defaults to the datacenter of the local agent")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_REGION)
                        .title("Nomad region")
                        .description("The region in which to execute the job.")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_GROUP_COUNT)
                        .title("Count")
                        .description("Number of container instances "
                                + "to be run on Nomad cluster")
                        .required(true)
                        .defaultValue("1")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .select(NOMAD_JOB_TYPE)
                        .title("Type of job")
                        .description("Specifies the Nomad scheduler to use.")
                        .required(true)
                        .values("batch",
                                "service"
                        )
                        .defaultValue("batch")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_MAX_FAIL_PCT)
                        .title("Max allowed failed instances, %")
                        .description("Maximum number of job allocations allowed to fail")
                        .required(true)
                        .defaultValue("0")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_ENV_VARS)
                        .title("Environment variables")
                        .description("A list of newline separated environment "
                                + "variable assignments. Example: FOO=foo\\nBAR=bar")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("displayType",
                                StringRenderingConstants.DisplayType.MULTI_LINE)
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_DYNAMIC_PORTS)
                        .title("Dynamic port labels")
                        .description("A comma-separated list of labels"
                                + " for dynamically allocated ports")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Networking")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_RESERVED_PORTS)
                        .title("Reserved port labels")
                        .description("A newline-separated key-value map of labels "
                                + "and values for statically allocated ports")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("displayType",
                                StringRenderingConstants.DisplayType.MULTI_LINE)
                        .renderingOption("groupName", "Networking")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_CPU)
                        .title("CPU limit")
                        .description("Specifies the CPU required to run this task in MHz")
                        .required(true)
                        .defaultValue("100")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_MEMORY)
                        .title("Memory limit")
                        .description("Specifies the memory required in MB")
                        .required(true)
                        .defaultValue("256")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_IOPS)
                        .title("IOPS limit")
                        .description("Specifies the number of IOPS required "
                                + "given as a weight between 0-1000.")
                        .required(true)
                        .defaultValue("0")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_NETWORK_BANDWIDTH)
                        .title("Network bandwidth, MBits")
                        .description("Specifies the bandwidth required in MBits")
                        .required(false)
                        .defaultValue("10")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                );
    }

}

