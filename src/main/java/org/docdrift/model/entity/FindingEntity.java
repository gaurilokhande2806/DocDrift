package org.docdrift.model.entity;

import jakarta.persistence.*;
import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.FindingStatus;
import org.docdrift.model.enums.MatchMethod;
import org.docdrift.model.enums.Severity;

@Entity
@Table(name = "findings")
public class FindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRunEntity analysisRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    private String docLocation;
    private String codeLocation;

    @Column(length = 1000)
    private String documentedValue;

    @Column(length = 1000)
    private String actualValue;

    @Column(length = 1000)
    private String difference;

    @Column(length = 1000)
    private String suggestion;

    private Double confidence;

    @Enumerated(EnumType.STRING)
    private MatchMethod matchMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingStatus status = FindingStatus.OPEN;

    private String firstSeenVersion;

    public FindingEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisRunEntity getAnalysisRun() {
        return analysisRun;
    }

    public void setAnalysisRun(AnalysisRunEntity analysisRun) {
        this.analysisRun = analysisRun;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDocLocation() {
        return docLocation;
    }

    public void setDocLocation(String docLocation) {
        this.docLocation = docLocation;
    }

    public String getCodeLocation() {
        return codeLocation;
    }

    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    public String getDocumentedValue() {
        return documentedValue;
    }

    public void setDocumentedValue(String documentedValue) {
        this.documentedValue = documentedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public MatchMethod getMatchMethod() {
        return matchMethod;
    }

    public void setMatchMethod(MatchMethod matchMethod) {
        this.matchMethod = matchMethod;
    }

    public FindingStatus getStatus() {
        return status;
    }

    public void setStatus(FindingStatus status) {
        this.status = status;
    }

    public String getFirstSeenVersion() {
        return firstSeenVersion;
    }

    public void setFirstSeenVersion(String firstSeenVersion) {
        this.firstSeenVersion = firstSeenVersion;
    }
}
