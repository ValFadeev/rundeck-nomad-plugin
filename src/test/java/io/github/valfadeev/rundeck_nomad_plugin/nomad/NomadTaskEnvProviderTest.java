package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import utils.TestConfigurationMapBuilder;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.NOMAD_ENV_VARS;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NomadTaskEnvProviderTest {

    @Test
    public void shouldSetEnvVars() throws Exception {

        final Map<String, Object> config = TestConfigurationMapBuilder.builder()
                .addItem(NOMAD_ENV_VARS, "FOO=bar\nABC=xyz")
                .getConfig();

        Map<String, String> env = NomadTaskEnvProvider.getEnv(config);

        Map<String, String> expectedEnv = new HashMap<>();
        expectedEnv.put("FOO", "bar");
        expectedEnv.put("ABC", "xyz");

        assertThat(env, is(expectedEnv));

    }

}