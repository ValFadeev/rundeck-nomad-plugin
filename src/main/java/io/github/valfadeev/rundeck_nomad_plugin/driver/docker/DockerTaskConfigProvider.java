package io.github.valfadeev.rundeck_nomad_plugin.driver.docker;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import io.github.valfadeev.rundeck_nomad_plugin.common.TaskConfigProvider;
import io.github.valfadeev.rundeck_nomad_plugin.util.ParseInput;

import static io.github.valfadeev.rundeck_nomad_plugin.driver.docker.DockerConfigOptions.*;

public class DockerTaskConfigProvider implements TaskConfigProvider {

    private Map<String, Object> configuration;

    public DockerTaskConfigProvider(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getConfig() {

        Map<String, Object> taskConfig = new HashMap<>();

        String dockerImage = this.configuration.get(DOCKER_IMAGE).toString();
        String dockerCommand = this.configuration.get(DOCKER_COMMAND).toString();
        String dockerLoad = this.configuration.get(DOCKER_LOAD).toString();
        Boolean dockerForcePull = Boolean.parseBoolean(this.configuration.get(DOCKER_FORCE_PULL).toString());
        String dockerArgString = this.configuration.get(DOCKER_ARGS).toString();
        String dockerLabelString = this.configuration.get(DOCKER_LABELS).toString();
        String dockerWorkDir = this.configuration.get(DOCKER_WORKDIR).toString();
        String dockerServer = this.configuration.get(DOCKER_SERVER_ADDRESS).toString();
        String dockerUsername = this.configuration.get(DOCKER_USERNAME).toString();
        String dockerPassword = this.configuration.get(DOCKER_PASSWORD).toString();
        String dockerEmail = this.configuration.get(DOCKER_EMAIL).toString();
        Boolean dockerInteractiveMode = Boolean.parseBoolean(this.configuration.get(DOCKER_INTERACTIVE_MODE).toString());
        Boolean dockerTty = Boolean.parseBoolean(this.configuration.get(DOCKER_TTY).toString());
        Boolean dockerAuthSoftFail = Boolean.parseBoolean(this.configuration.get(DOCKER_AUTH_SOFT_FAIL).toString());
        String dockerLogDriver = this.configuration.get(DOCKER_LOG_DRIVER).toString();
        String dockerLogOpt = this.configuration.get(DOCKER_LOG_OPT).toString();
        String dockerNetworkMode = this.configuration.get(DOCKER_NETWORK_MODE).toString();
        String dockerNetworkAliasString = this.configuration.get(DOCKER_NETWORK_ALIASES).toString();
        String dockerPortMapString = this.configuration.get(DOCKER_PORT_MAP).toString();
        String dockerHostName = this.configuration.get(DOCKER_HOSTNAME).toString();
        String dockerIpv4Address = this.configuration.get(DOCKER_IPV4_ADDRESS).toString();
        String dockerIpv6Address = this.configuration.get(DOCKER_IPV6_ADDRESS).toString();
        String dockerMacAddress = this.configuration.get(DOCKER_MAC_ADDRESS).toString();
        String dockerExtraHostsString = this.configuration.get(DOCKER_EXTRA_HOSTS).toString();
        String dockerDnsSearchDomainsString = this.configuration.get(DOCKER_DNS_SEARCH_DOMAINS).toString();
        String dockerDnsServersString = this.configuration.get(DOCKER_DNS_SERVERS).toString();
        String dockerVolumesString = this.configuration.get(DOCKER_VOLUMES).toString();
        String dockerVolumeDriver = this.configuration.get(DOCKER_VOLUME_DRIVER).toString();
        String dockerShmSize = this.configuration.get(DOCKER_SHM_SIZE).toString();
        String dockerSecurityOptString = this.configuration.get(DOCKER_SECURITY_OPT).toString();
        String dockerIpcMode = this.configuration.get(DOCKER_IPC_MODE).toString();
        String dockerPidMode = this.configuration.get(DOCKER_PID_MODE).toString();
        String dockerUtsMode = this.configuration.get(DOCKER_UTS_MODE).toString();
        String dockerUsernsMode = this.configuration.get(DOCKER_USERNS_MODE).toString();
        Boolean dockerPrivilegedMode = Boolean.parseBoolean(this.configuration.get(DOCKER_PRIVILEGED_MODE).toString());

        taskConfig.put("ipc_mode", dockerIpcMode);
        taskConfig.put("pid_mode", dockerPidMode);
        taskConfig.put("uts_mode", dockerUtsMode);
        taskConfig.put("userns_mode", dockerUsernsMode);
        taskConfig.put("privileged", dockerPrivilegedMode);
        taskConfig.put("image", dockerImage);
        taskConfig.put("auth_soft_fail", dockerAuthSoftFail);
        taskConfig.put("force_pull", dockerForcePull);
        taskConfig.put("command", dockerCommand);
        taskConfig.put("interactive", dockerInteractiveMode);
        taskConfig.put("tty", dockerTty);
        taskConfig.put("network_mode", dockerNetworkMode);

        if (!dockerWorkDir.isEmpty()) {
            taskConfig.put("work_dir", dockerWorkDir);
        }

        if (!dockerHostName.isEmpty()) {
            taskConfig.put("hostname", dockerHostName);
        }

        if (!dockerLoad.isEmpty()) {
            taskConfig.put("load", dockerLoad);
        }

        if (!dockerArgString.isEmpty()) {
            String[] dockerArgs = dockerArgString.split(",");
            taskConfig.put("args", dockerArgs);
        }

        if (!dockerLabelString.isEmpty()) {
            List<Map<String, String>> labelList = new ArrayList<>();
            labelList.add(ParseInput.kvToMap(dockerLabelString));
            taskConfig.put("labels", labelList);
        }

        if (!dockerIpv4Address.isEmpty()) {
            taskConfig.put("ipv4_address", dockerIpv4Address);
        }

        if (!dockerIpv6Address.isEmpty()) {
            taskConfig.put("ipv6_address", dockerIpv6Address);
        }

        if (!dockerMacAddress.isEmpty()) {
            taskConfig.put("mac_address", dockerMacAddress);
        }

        if (!dockerNetworkAliasString.isEmpty()) {
            String[] dockerNetworkAliases = dockerNetworkAliasString.split(",");
            taskConfig.put("network_aliases", dockerNetworkAliases);
        }

        if (!dockerExtraHostsString.isEmpty()) {
            String[] dockerExtraHosts = dockerExtraHostsString.split(",");
            taskConfig.put("extra_hosts", dockerExtraHosts);
        }

        if (!dockerDnsSearchDomainsString.isEmpty()) {
            String[] dockerDnsSearchDomains = dockerDnsSearchDomainsString.split(",");
            taskConfig.put("dns_search_domains", dockerDnsSearchDomains);
        }

        if (!dockerDnsServersString.isEmpty()) {
            String[] dockerDnsServers = dockerDnsServersString.split(",");
            taskConfig.put("dns_servers", dockerDnsServers);
        }

        if (!dockerVolumesString.isEmpty()) {
            taskConfig.put("volumes", dockerVolumesString.split("\n"));
        }

        if (!dockerVolumeDriver.isEmpty()) {
            taskConfig.put("volume_driver", dockerVolumeDriver);
        }

        if (!dockerShmSize.isEmpty()) {
            taskConfig.put("shm_size", Integer.parseInt(dockerShmSize));
        }

        if (!dockerSecurityOptString.isEmpty()) {
            taskConfig.put("security_opt", dockerSecurityOptString.split(","));
        }

        Map<String, String> dockerAuth = new HashMap<>();
        if (!dockerServer.isEmpty()) {
            dockerAuth.put("server_address", dockerServer);
        }
        if (!dockerUsername.isEmpty()) {
            dockerAuth.put("username", dockerUsername);
        }
        if (!dockerPassword.isEmpty()) {
            dockerAuth.put("password", dockerPassword);
        }
        if (!dockerEmail.isEmpty()) {
            dockerAuth.put("email", dockerEmail);
        }
        if (dockerAuth.size() > 0) {
            taskConfig.put("auth", dockerAuth);
        }

        if (!dockerLogDriver.isEmpty()) {
            Map<String, Object> logging = new HashMap<>();
            logging.put("type", dockerLogDriver);

            if (!dockerLogOpt.isEmpty()) {
                List<Map<String, String>> logOptList = new ArrayList<>();
                logOptList.add(ParseInput.kvToMap(dockerLogOpt));
                logging.put("config", logOptList);
            }

            List<Map<String, Object>> loggingArr = new ArrayList<>();
            loggingArr.add(logging);
            taskConfig.put("logging", loggingArr);
        }

        if (!dockerPortMapString.isEmpty()) {
            List<Map<String, String >> portMapList = new ArrayList<>();
            portMapList.add(ParseInput.kvToMap(dockerPortMapString));
            taskConfig.put("port_map", portMapList);
        }

        return taskConfig;

    }
}
