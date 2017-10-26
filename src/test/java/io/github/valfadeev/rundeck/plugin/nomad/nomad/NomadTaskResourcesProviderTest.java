package io.github.valfadeev.rundeck.plugin.nomad.nomad;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hashicorp.nomad.apimodel.Port;
import org.junit.Test;
import utils.TestConfigurationMapBuilder;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.hashicorp.nomad.apimodel.Resources;


public class NomadTaskResourcesProviderTest {

    @Test
    public void shouldSetProperties() throws Exception {
        final Map<String, Object> config = TestConfigurationMapBuilder.builder()
                .addItem(NomadConfigOptions.NOMAD_TASK_CPU, "50")
                .addItem(NomadConfigOptions.NOMAD_TASK_MEMORY, "512")
                .addItem(NomadConfigOptions.NOMAD_TASK_IOPS, "100")
                .addItem(NomadConfigOptions.NOMAD_NETWORK_BANDWIDTH, "10")
                .addItem(NomadConfigOptions.NOMAD_DYNAMIC_PORTS, "http,https")
                .addItem(NomadConfigOptions.NOMAD_RESERVED_PORTS, "amqp=5672\ndb=6379")
                .getConfig();

        final Resources resources = NomadTaskResourcesProvider
                .getResources(config);

        final Set<String> dynamicPortLabels = resources.getNetworks()
                .get(0)
                .getDynamicPorts()
                .stream()
                .map(Port::getLabel)
                .collect(Collectors.toSet());
        final Set<String> expectedDynamicPortLabels
                = new HashSet<>(Arrays.asList("http","https"));

        final Map<String, Integer> reservedPortMap = resources.getNetworks()
                .get(0)
                .getReservedPorts()
                .stream()
                .collect(Collectors.toMap(
                        Port::getLabel,
                        Port::getValue));

        final Map<String, Integer> expectedReservedPortMap = new HashMap<>();
        expectedReservedPortMap.put("db", 6379);
        expectedReservedPortMap.put("amqp", 5672);


        assertThat(resources.getCpu(), is(50));
        assertThat(resources.getMemoryMb(), is(512));
        assertThat(resources.getIops(), is(100));
        assertThat(resources.getNetworks().get(0).getMBits(), is(10));

        assertThat(dynamicPortLabels, is(expectedDynamicPortLabels));
        assertThat(reservedPortMap, is(expectedReservedPortMap));

    }


}