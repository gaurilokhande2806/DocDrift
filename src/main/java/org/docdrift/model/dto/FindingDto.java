package org.docdrift.model.dto;

import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.FindingStatus;
import org.docdrift.model.enums.MatchMethod;
import org.docdrift.model.enums.Severity;

public class FindingDto {
    private Long id;
    private Category category;
    private Severity severity;
    private String docLocation;
    private String codeLocation;
    private String documentedValue;
    private String actualValue;
    private String difference;
    private String suggestion;
    private Double confidence;
    private MatchMethod matchMethod;
    private FindingStatus status;
    private String firstSeenVersion;

    public FindingDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
