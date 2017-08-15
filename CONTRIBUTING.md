# Contributing

Since both Rundeck and Nomad are fast-evolving projects, work will need to be done to keep the plugin up-to-date. An important part of that is maintaining good coverage of [driver](https://www.nomadproject.io/docs/drivers/index.html) functionality. Drivers are implemented as separate plugin classes shipped in the same jar, however, they share a lot of logic and inherit from the same class. Adding a new driver amounts to defining input fields in the UI and mapping them onto the driver-specific configuration keys in the job definition. An outline of a new driver implementation is given below.

## Adding a new driver

To add support for a new task driver, for example `jar` driver, follow these steps
  * Create a subpackage e.g. `io.github.valfadeev.rundeck_nomad_plugin.driver.jar` and implement `JarPropertyComposer` (for UI fields) and `JarTaskConfigProvider` (for mapping onto task configuration). Please maintain the naming convention, because the plugin base class uses reflection to load the driver-specific classes. Using constants for configuration option keys is not necessary, but is highly recommended for easier debugging and refactoring.
  * Add new value to `SupportedDrivers`
  * Create new class `NomadJarStepPlugin` in the package root extending `NomadStepPlugin` and annotate it with `@Plugin`, `@PluginDescription` and `@Driver`. Define static field `SERVICE_PROVIDER_NAME`. Rely on the `NomadDockerStepPlugin` as the example.
  * Add the fully qualified class name to `ext.pluginClassNames` in `build.gradle`.
  * Add tests
  * Build the project and verify that all plugins are correctly loaded by Rundeck.
