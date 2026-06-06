package com.fak.policy;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies/versions")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class PolicyVersionController {

    private final PolicyVersionService service;

    public PolicyVersionController(PolicyVersionService service) {
        this.service = service;
    }

    /** List all policy versions, newest first. */
    @GetMapping
    public List<PolicyVersion> list() {
        return service.list();
    }

    /** Get the currently active policy version. */
    @GetMapping("/active")
    public PolicyVersion active() {
        return service.getActive()
                .orElseThrow(() -> new IllegalStateException("No active policy version found"));
    }

    /** Get a specific version by DB id. */
    @GetMapping("/{id}")
    public PolicyVersion getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Create a new DRAFT version.
     * Body: { "yamlContent": "...", "description": "...", "author": "..." }
     */
    @PostMapping
    public PolicyVersion createDraft(@RequestBody Map<String, String> body) throws IOException {
        return service.createDraft(
                body.get("yamlContent"),
                body.get("description"),
                body.get("author"));
    }

    /**
     * Promote a DRAFT to ACTIVE (archives current ACTIVE, hot-reloads the kernel).
     */
    @PostMapping("/{id}/activate")
    public Map<String, Object> activate(@PathVariable Long id) throws IOException {
        PolicyVersion activated = service.activate(id);
        return Map.of(
                "activated", activated.getVersionId(),
                "status", "ACTIVE",
                "message", "Policy hot-reloaded. All subsequent validations use the new version.");
    }
}
