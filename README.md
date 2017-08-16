# Rundeck Nomad Plugin

**This is early work. Use with extreme caution!**

## Purpose
This is a [Workflow Step](http://rundeck.org/docs/developer/workflow-step-plugin.html) plugin for submitting jobs to a [Nomad](https://www.nomadproject.io/intro/index.html#what-is-nomad-) cluster via Rundeck UI. The plugin interacts with a Nomad server via HTTP API.

Rundeck is a popular and well established automation tool. It features, besides other things, a rich customizable UI, role-based access control, scheduling, logging, alerts, cli and API support and an already extensive plugin ecosystem. It fits well into the CI/CD pipelines (for instance, there is a [Jenkins](http://rundeck.org/plugins/2013/01/01/jenkins-rundeck.html) Rundeck integration plugin). Rundeck [does not](http://rundeck.org/docs/administration/scaling-rundeck.html) lend itself easily to running in HA mode or scaling worker nodes. Therefore, it seems a good idea put a distributed scheduler such as Nomad behind it to offload resource-intensive jobs.

![Alt Screenshot](/images/job.png)

## Installation
  * Download and start [Rundeck](http://rundeck.org/downloads.html). It will automatically create the necessary directories.
  * Clone this repository. Test and build using `gradle` wrapper:
    ```
      ./gradlew test
      ./gradlew build
    ```
  * Drop `rundeck-nomad-plugin-<version>.jar` to `libext/` under Rundeck installation directory.
  * Restart Rundeck.

## Usage
Download [Nomad](https://www.nomadproject.io/docs/agent/index.html#running-an-agent) and start an agent in server mode. For evaluation you may use [development mode](https://www.nomadproject.io/docs/agent/index.html#running-an-agent) for zero-configuration start.

In Rundeck UI create a new project and a new job in that project. Under _"Add a Step"_ section swich to _"Workflow Steps"_ tab. If the plugin was recognized successfully, you should see _"Run Docker container on Nomad"_ in the list of the available workflow step plugins. Click on the plugin entry to bring up the input form, fill in Nomad agent URL, docker image name and any other available fields. Save and run the job.

## What is in scope
Currently the scope is limited to [batch](https://www.nomadproject.io/docs/runtime/schedulers.html#batch) jobs of simple structure (1 job, 1 task group, 1 task). The reason is such jobs fit well into the Rundeck operating model and map onto the available UI configuration in a straightforward way. It is possible to set the task count within the task group thereby increasing parallelism where that matters.

Nomad supports a range of [Drivers](https://www.nomadproject.io/docs/drivers/index.html) to execute tasks. At the moment only Docker driver task configuration is supported by the plugin. However, best effort has been made to isolate driver-specific code and make the extension process simple.

## Job lifecycle
Monitoring of the running jobs is performed in several stages the outcome of which is reported in the log output. Please consult [Nomad documentation](https://www.nomadproject.io/docs/internals/scheduling.html) for the relevant terminology. First it is checked if the job has been successfully submitted to the scheduler. Then it is verified if the job passed the evaluation (evaluation ID is reported). Depending on the desired task count the corresponding number of allocations will be placed by Nomad. Some or all of the allocations may fail for various reasons (resource limitations, driver error, etc), however, the job as a whole can only have _pending_, _running_ or _dead_ status which may not be representative of the success/failure of the outcome. Hence, in order to allow for some flexibility, we poll for the status of the individual allocations and raise an error if more than a configurable percentage of them end up in a _failed_ status.

Note that logs from individual tasks are *not* streamed here. Given the arbitrary number of task instances that can be deployed it could be challenging to read all of their streams into Rundeck output. Some support for that may be added in future.

Nomad supports scheduling of [periodic](https://www.nomadproject.io/docs/job-specification/periodic.html) jobs and defining [restart](https://www.nomadproject.io/docs/job-specification/restart.html) policies, and also Nomad SDK implements [time-outs](https://github.com/hashicorp/nomad-java-sdk/blob/master/sdk/src/main/java/com/hashicorp/nomad/javasdk/WaitStrategy.java) and back-off strategy for all API calls. However, all of the above settings also belong to core functionality of Rundeck. Therefore, in order to avoid confusion, it was decided to delegate them to Rundeck job-level configuration. That is why API calls are configured to wait indefinitely and _periodic_ stanza from Nomad job specification is not supported. It may be implemented in future, if this plugin is enhanced to be able to deploy long running services.

![Alt Screenshot](/images/log.png)

## Minimal version requirements
  * Java 1.8
  * Rundeck 2.9.x
  * Nomad 0.6.0

## Similar projects
As Nomad gains popularity a number of projects providing UI for visual cluster management or using Nomad as a backend for job scheduling already exist:
  * [cvandal/nomad-ui](https://github.com/cvandal/nomad-ui) - UI for Nomad by HashiCorp.
  * [jippi/hashi-ui](https://github.com/jippi/hashi-ui) - UI for Nomad and [Consul](https://www.consul.io/docs/index.html).
  * [FRosner/cluster-broccoli](https://github.com/FRosner/cluster-broccoli) - templated UI for configuring jobs.
  * [Verizon/nelson](https://verizon.github.io/nelson/) - container deployment manager, supporting Nomad as a backend.

## Thanks
  * [rundeck-mesos-plugin](https://github.com/farmapromlab/rundeck-mesos-plugin) provided a lot of useful examples and ideas for the initial structure

## TODO
  * Better test coverage
  * Driver support
  * More detailed logging
  * TLS support
  * Contraints configuration

