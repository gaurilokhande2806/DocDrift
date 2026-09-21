package org.docdrift.model.dto;

import org.docdrift.model.enums.DecayLevel;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResponse {
    private Long runId;
    private Long projectId;
    private String projectName;
    private String version;
    private Double overallDhs;
    private DecayLevel decayLevel;
    private Integer totalVerifiableElements;
    private Integer totalFindings;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status;
    private List<FindingDto> findings;

    public AnalysisResponse() {
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public List<FindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<FindingDto> findings) {
        this.findings = findings;
    }
}
