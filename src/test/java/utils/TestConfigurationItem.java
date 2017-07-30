package utils;

public class TestConfigurationItem {
    private String value;

    public TestConfigurationItem(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
