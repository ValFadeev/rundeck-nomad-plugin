package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.List;
import java.util.Map;

import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.Resources;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;
        
public class NomadTaskResourcesProvider {

    public static Resources getResources(Map<String, Object> configuration) {

        Resources resourceConfig = new Resources();

        int cpu = Integer.parseInt(configuration
                .get(NOMAD_TASK_CPU)
                .toString());
        resourceConfig.setCpu(cpu);

        int memory = Integer.parseInt(configuration
                .get(NOMAD_TASK_MEMORY)
                .toString());
        resourceConfig.setMemoryMb(memory);

        int iops = Integer.parseInt(configuration
                .get(NOMAD_TASK_IOPS)
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
