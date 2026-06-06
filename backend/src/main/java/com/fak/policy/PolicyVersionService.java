package com.fak.policy;

import com.fak.config.ConfigRegistry;
import com.fak.config.PolicyConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyVersionService {

    private final PolicyVersionRepository repo;
    private final ConfigRegistry registry;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

    @Value("${fak.policy-path}")
    private Resource policyResource;

    public PolicyVersionService(PolicyVersionRepository repo, ConfigRegistry registry) {
        this.repo = repo;
        this.registry = registry;
    }

    /** On first startup, seed the DB with the policies.yml file as version 1. */
    @PostConstruct
    @Transactional
    public void init() throws IOException {
        if (repo.count() == 0) {
            String content;
            try (InputStream is = policyResource.getInputStream()) {
                content = new String(is.readAllBytes());
            }
            PolicyVersion v = new PolicyVersion();
            v.setVersionId(registry.current().policyVersion());
            v.setYamlContent(content);
            v.setDescription("Initial policy — seeded from policies.yml");
            v.setAuthor("system");
            v.setStatus("ACTIVE");
            v.setCreatedAt(LocalDateTime.now());
            repo.save(v);
        }
    }

    public List<PolicyVersion> list() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Optional<PolicyVersion> getActive() {
        return repo.findFirstByStatus("ACTIVE");
    }

    public PolicyVersion getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy version not found: " + id));
    }

    /** Create a new DRAFT version. Validates the YAML before saving. */
    @Transactional
    public PolicyVersion createDraft(String yamlContent, String description, String author) throws IOException {
        // Validate YAML parses into a PolicyConfig
        yaml.readValue(yamlContent, PolicyConfig.class);

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmm"));
        PolicyVersion v = new PolicyVersion();
        v.setVersionId("pol_" + stamp);
        v.setYamlContent(yamlContent);
        v.setDescription(description != null ? description : "New draft policy");
        v.setAuthor(author != null ? author : "user");
        v.setStatus("DRAFT");
        v.setCreatedAt(LocalDateTime.now());
        return repo.save(v);
    }

    /** Promote a DRAFT to ACTIVE, archive the current ACTIVE version, and hot-reload ConfigRegistry. */
    @Transactional
    public PolicyVersion activate(Long id) throws IOException {
        PolicyVersion toActivate = getById(id);

        // Archive the current active version
        repo.findFirstByStatus("ACTIVE").ifPresent(current -> {
            current.setStatus("ARCHIVED");
            repo.save(current);
        });

        toActivate.setStatus("ACTIVE");
        repo.save(toActivate);

        // Hot-reload the running policy without a restart
        PolicyConfig newConfig = yaml.readValue(toActivate.getYamlContent(), PolicyConfig.class);
        registry.reload(newConfig);

        return toActivate;
    }
}
