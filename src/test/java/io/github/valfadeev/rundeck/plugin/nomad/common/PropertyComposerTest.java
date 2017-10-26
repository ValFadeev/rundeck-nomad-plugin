package io.github.valfadeev.rundeck.plugin.nomad.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import io.github.valfadeev.rundeck.plugin.nomad.driver.docker.DockerConfigOptions;
import io.github.valfadeev.rundeck.plugin.nomad.nomad.NomadConfigOptions;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyComposerTest {
    @Test
    public void shouldConcatenateProperties() throws Exception {

        class Composer1 extends PropertyComposer{
            @Override
            protected DescriptionBuilder addProperties(DescriptionBuilder builder) {
                return builder
                        .property(PropertyBuilder.builder()
                                .string(NomadConfigOptions.NOMAD_URL)
                                .title("Nomad agent URL")
                                .description("URL of the Nomad agent to submit job to")
                                .required(true)
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .string(NomadConfigOptions.NOMAD_DATACENTER)
                                .title("Nomad datacenter")
                                .description("A list of datacenters in the region "
                                        + "which are eligible for task placement. "
                                        + "Defaults to the datacenter of the local agent")
                                .required(false)
                                .defaultValue("")
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .string(NomadConfigOptions.NOMAD_REGION)
                                .title("Nomad region")
                                .description("The region in which to execute the job.")
                                .required(false)
                                .defaultValue("")
                                .build()
                        );

            }
        }

        class Composer2 extends PropertyComposer{
            @Override
            protected DescriptionBuilder addProperties(DescriptionBuilder builder) {
                return builder
                        .property(PropertyBuilder.builder()
                                .string(DockerConfigOptions.DOCKER_IMAGE)
                                .title("Docker image")
                                .description("The Docker image to run")
                                .required(true)
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .string(DockerConfigOptions.DOCKER_LOAD)
                                .title("Load image from file")
                                .description("Path to image archive file")
                                .required(false)
                                .defaultValue("")
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .booleanType(DockerConfigOptions.DOCKER_FORCE_PULL)
                                .title("Force pull")
                                .description("Always pull latest image instead of "
                                        + "using existing local image")
                                .required(false)
                                .defaultValue("false")
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .string(DockerConfigOptions.DOCKER_SERVER_ADDRESS)
                                .title("Server")
                                .description("The server domain/IP without the protocol. "
                                        + "Docker Hub is used by default.")
                                .required(false)
                                .defaultValue("")
                                .renderingOption("groupName", "Registry")
                                .renderingOption("grouping", "secondary")
                                .build()
                        )
                        .property(PropertyBuilder.builder()
                                .string(DockerConfigOptions.DOCKER_USERNAME)
                                .title("Username")
                                .description("The account username.")
                                .renderingOption("selectionAccessor",
                                        StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
                                .renderingOption("valueConversion",
                                        StringRenderingConstants.ValueConversion.STORAGE_PATH_AUTOMATIC_READ)
                                .required(false)
                                .defaultValue("")
                                .renderingOption("groupName", "Registry")
                                .renderingOption("grouping", "secondary")
                                .build()
                        );
            }
        }

        Composer1 c1 = new Composer1();
        List<String> p1 = c1
                .getBuilder().name("test1")
                .description("test1")
                .build()
                .getProperties()
                .stream()
                .map(Property::getName)
                .collect(Collectors.toList());

        Composer2 c2 = new Composer2();
        List<String> p2 = c2
                .getBuilder().name("test2")
                .description("test2")
                .build()
                .getProperties()
                .stream()
                .map(Property::getName)
                .collect(Collectors.toList());

        List<String> pa = new ArrayList<>(p1);
        List<String> pb = new ArrayList<>(p2);

        pb.addAll(pa);

        List<String> pd = c1.compose(c2)
                .getBuilder()
                .build()
                .getProperties()
                .stream()
                .map(Property::getName)
                .collect(Collectors.toList());

        assertArrayEquals(pd.toArray(), pb.toArray());
    }

    @Test
    public void compose() throws Exception {
    }

    @Test
    public void addProperties() throws Exception {
    }

}