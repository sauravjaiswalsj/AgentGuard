package com.fak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ConfigRegistry {
    private volatile PolicyConfig config;

    public ConfigRegistry(@Value("${fak.policy-path}") Resource policyResource) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
        try (InputStream input = policyResource.getInputStream()) {
            this.config = mapper.readValue(input, PolicyConfig.class);
        }
    }

    public PolicyConfig current() {
        return config;
    }

    public AgentSpec agent(String agentId) {
        return config.agents().get(agentId);
    }

    /**
     * Hot-reload the active policy without a server restart.
     * Called by PolicyVersionService when a new version is activated.
     * Volatile field ensures visibility across threads.
     */
    public synchronized void reload(PolicyConfig newConfig) {
        this.config = newConfig;
    }
}

// hot-reload supported since pol_2026_07_27
