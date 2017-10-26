package io.github.valfadeev.rundeck.plugin.nomad.driver.java;

import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import io.github.valfadeev.rundeck.plugin.nomad.common.PropertyComposer;

public class JavaPropertyComposer  extends PropertyComposer {

    @Override
    public DescriptionBuilder addProperties(DescriptionBuilder builder) {
        return builder
                .property(PropertyBuilder.builder()
                    .string(JavaConfigOptions.JAVA_CLASS)
                    .title("Class name")
                    .description("The name of the class to run.")
                    .required(false)
                    .defaultValue("")
                    .build()
                )
                .property(PropertyBuilder.builder()
                        .string(JavaConfigOptions.JAVA_CLASS_PATH)
                        .title("Class path")
                        .description("Specifies the path for Java "
                                + "to look for classes.")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(JavaConfigOptions.JAVA_JAR_PATH)
                        .title("Jar path")
                        .description("Path to the downloaded Jar.")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(JavaConfigOptions.JAVA_ARGS)
                        .title("Arguments")
                        .description("A comma-separated list of "
                                + "arguments to the Jar's"
                                + "main method.")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(JavaConfigOptions.JAVA_JVM_OPTIONS)
                        .title("JVM options")
                        .description("A comma-separated list of JVM "
                                + "options to be passed"
                                + "while invoking java.")
                        .required(false)
                        .defaultValue("")
                        .build()
                );
    }
}
