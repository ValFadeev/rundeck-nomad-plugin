package io.github.valfadeev.rundeck.plugin.nomad.nomad;

import java.util.List;
import java.util.Map;

import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.Resources;

public class NomadTaskResourcesProvider {

    public static Resources getResources(Map<String, Object> configuration) {

        Resources resourceConfig = new Resources();

        int cpu = Integer.parseInt(configuration
                .get(NomadConfigOptions.NOMAD_TASK_CPU)
                .toString());
        resourceConfig.setCpu(cpu);

        int memory = Integer.parseInt(configuration
                .get(NomadConfigOptions.NOMAD_TASK_MEMORY)
                .toString());
        resourceConfig.setMemoryMb(memory);

        int iops = Integer.parseInt(configuration
                .get(NomadConfigOptions.NOMAD_TASK_IOPS)
                .toString());
        resourceConfig.setIops(iops);

        List<NetworkResource> networks = NomadTaskNetworkResourcesProvider
                .getNetworkResources(configuration);

        if (networks.size()>0) {
            resourceConfig.setNetworks(networks);
        }

        return resourceConfig;
    }
}
