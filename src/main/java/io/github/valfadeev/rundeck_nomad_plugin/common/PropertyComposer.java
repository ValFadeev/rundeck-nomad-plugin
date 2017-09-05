package io.github.valfadeev.rundeck_nomad_plugin.common;

import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

public class PropertyComposer {

    private DescriptionBuilder builder;

    public PropertyComposer() {
        this.builder = DescriptionBuilder.builder();
    }

    protected DescriptionBuilder addProperties(DescriptionBuilder builder) {
        return builder;
    }

    public DescriptionBuilder getBuilder() {
        return this.addProperties(builder);
    }

    public PropertyComposer compose(PropertyComposer other) {
        this.builder = other.getBuilder();
        return this;
    }
}
