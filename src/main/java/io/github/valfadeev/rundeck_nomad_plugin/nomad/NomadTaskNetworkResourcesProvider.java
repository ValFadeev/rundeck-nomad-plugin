package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.Port;
import io.github.valfadeev.rundeck_nomad_plugin.util.ParseInput;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;

public class NomadTaskNetworkResourcesProvider {

    public static List<NetworkResource> getNetworkResources(Map<String, Object> configuration) {

        NetworkResource network = new NetworkResource();

        int bandwidth = Integer.parseInt(configuration
                .get(NOMAD_NETWORK_BANDWIDTH)
                .toString());
        if (bandwidth > 0) {
            network.setMBits(bandwidth);
        }

        String dynamicPortString = configuration
                .get(NOMAD_DYNAMIC_PORTS)
                .toString();
        if (!dynamicPortString.isEmpty()) {
            List<Port> dynamicPorts = Arrays.stream(
                            dynamicPortString
                            .split(","))
                    .map(s -> new Port()
                            .setLabel(s))
                    .collect(Collectors.toList());
            network.setDynamicPorts(dynamicPorts);
        }

        String reservedPortString = configuration
                .get(NOMAD_RESERVED_PORTS)
                .toString();
        if (!reservedPortString.isEmpty()) {
            List<Port> reservedPorts = ParseInput
                    .kvToMap(reservedPortString)
                    .entrySet()
                    .stream()
                    .map(m -> new Port()
                            .setLabel(m.getKey())
                            .setValue(Integer.parseInt(m.getValue())))
                    .collect(Collectors.toList());
            network.setReservedPorts(reservedPorts);
        }

        List<NetworkResource> networks = new ArrayList<>();
        networks.add(network);

        return networks;
    }
}
