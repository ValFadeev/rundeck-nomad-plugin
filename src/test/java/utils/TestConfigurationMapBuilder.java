package utils;

import java.util.HashMap;
import java.util.Map;

public class TestConfigurationMapBuilder {

    private Map<String, Object> config = new HashMap<>();

    public static TestConfigurationMapBuilder builder() {
        return new TestConfigurationMapBuilder();
    }

    public TestConfigurationMapBuilder addItem(String key, String value) {
        this.config.put(key, new TestConfigurationItem(value));
        return this;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
