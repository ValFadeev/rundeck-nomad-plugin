package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.Resources;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskGroup;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;

public class NomadJobProvider {

    public static Job getJob(Map<String, Object> configuration,
                             Map<String, Object> agentConfig,
                             Map<String, Object> taskConfig,
                             String driver,
                             String id,
                             String name,
                             String taskGroupName) {

        Map<String, String> env = NomadTaskEnvProvider.getEnv(configuration);
        Resources resources = NomadTaskResourcesProvider.getResources(configuration);

        Task task = new Task()
                .setConfig(taskConfig)
                .setResources(resources)
                .setEnv(env)
                .setName(name)
                .setDriver(driver);

        int groupCount = Integer.parseInt(configuration
                .get(NOMAD_GROUP_COUNT)
                .toString());
        TaskGroup group = new TaskGroup()
                .setName(taskGroupName)
                .setCount(groupCount)
                .addTasks(task);

        String datacenter = configuration
                .get(NOMAD_DATACENTER)
                .toString();
        List<String> datacenters = new ArrayList<>();
        if (datacenter.isEmpty()) {
            datacenters.add(agentConfig.get("Datacenter").toString());
        } else {
            String[] dcs = datacenter.split(",");
            datacenters.addAll(Arrays.asList(dcs));
        }

        String region = configuration
                .get(NOMAD_REGION)
                .toString();
        if (region.isEmpty()) {
            region = agentConfig.get("Region").toString();
        }

        String jobType = configuration.get(NOMAD_JOB_TYPE).toString();

        return new Job()
                .setId(id)
                .setName(name)
                .setType(jobType)
                .setDatacenters(datacenters)
                .setRegion(region)
                .addTaskGroups(group);
    }
}
