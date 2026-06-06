package com.fak.policy;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_versions")
public class PolicyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String versionId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String yamlContent;

    private String description;
    private String author;

    @Column(nullable = false)
    private String status; // DRAFT | ACTIVE | ARCHIVED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getVersionId() { return versionId; }
    public void setVersionId(String v) { this.versionId = v; }
    public String getYamlContent() { return yamlContent; }
    public void setYamlContent(String v) { this.yamlContent = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getAuthor() { return author; }
    public void setAuthor(String v) { this.author = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
