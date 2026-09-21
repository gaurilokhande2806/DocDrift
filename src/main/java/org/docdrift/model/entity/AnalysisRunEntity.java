package org.docdrift.model.entity;

import jakarta.persistence.*;
import org.docdrift.model.enums.DecayLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_runs")
public class AnalysisRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    private String version;

    private Double overallDhs;

    @Enumerated(EnumType.STRING)
    private DecayLevel decayLevel;

    private Integer totalVerifiableElements;

    private Integer totalFindings;

    private Double apiConsistencyScore;
    private Double dbConsistencyScore;
    private Double readmeConsistencyScore;
    private Double javadocConsistencyScore;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private String status; // QUEUED, RUNNING, COMPLETED, FAILED

    public AnalysisRunEntity() {
    }

    public AnalysisRunEntity(ProjectEntity project, String version) {
        this.project = project;
        this.version = version;
        this.startedAt = LocalDateTime.now();
        this.status = "RUNNING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Double getOverallDhs() {
        return overallDhs;
    }

    public void setOverallDhs(Double overallDhs) {
        this.overallDhs = overallDhs;
    }

    public DecayLevel getDecayLevel() {
        return decayLevel;
    }

    public void setDecayLevel(DecayLevel decayLevel) {
        this.decayLevel = decayLevel;
    }

    public Integer getTotalVerifiableElements() {
        return totalVerifiableElements;
    }

    public void setTotalVerifiableElements(Integer totalVerifiableElements) {
        this.totalVerifiableElements = totalVerifiableElements;
    }

    public Integer getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(Integer totalFindings) {
        this.totalFindings = totalFindings;
    }

    public Double getApiConsistencyScore() {
        return apiConsistencyScore;
    }

    public void setApiConsistencyScore(Double apiConsistencyScore) {
        this.apiConsistencyScore = apiConsistencyScore;
    }

    public Double getDbConsistencyScore() {
        return dbConsistencyScore;
    }

    public void setDbConsistencyScore(Double dbConsistencyScore) {
        this.dbConsistencyScore = dbConsistencyScore;
    }

    public Double getReadmeConsistencyScore() {
        return readmeConsistencyScore;
    }

    public void setReadmeConsistencyScore(Double readmeConsistencyScore) {
        this.readmeConsistencyScore = readmeConsistencyScore;
    }

    public Double getJavadocConsistencyScore() {
        return javadocConsistencyScore;
    }

    public void setJavadocConsistencyScore(Double javadocConsistencyScore) {
        this.javadocConsistencyScore = javadocConsistencyScore;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
