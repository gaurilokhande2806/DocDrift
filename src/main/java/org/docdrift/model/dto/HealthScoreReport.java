package org.docdrift.model.dto;

import org.docdrift.model.enums.DecayLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthScoreReport {
    private double overallDhs;
    private DecayLevel decayLevel;
    private int totalVerifiableElements;
    private int totalFindings;
    private double weightedSum;
    
    private Map<String, Double> categoryScores = new HashMap<>();
    private List<FindingDto> findings = new ArrayList<>();

    public HealthScoreReport() {
    }

    public HealthScoreReport(double overallDhs, DecayLevel decayLevel, int totalVerifiableElements, int totalFindings, double weightedSum) {
        this.overallDhs = overallDhs;
        this.decayLevel = decayLevel;
        this.totalVerifiableElements = totalVerifiableElements;
        this.totalFindings = totalFindings;
        this.weightedSum = weightedSum;
    }

    public double getOverallDhs() {
        return overallDhs;
    }

    public void setOverallDhs(double overallDhs) {
        this.overallDhs = overallDhs;
    }

    public DecayLevel getDecayLevel() {
        return decayLevel;
    }

    public void setDecayLevel(DecayLevel decayLevel) {
        this.decayLevel = decayLevel;
    }

    public int getTotalVerifiableElements() {
        return totalVerifiableElements;
    }

    public void setTotalVerifiableElements(int totalVerifiableElements) {
        this.totalVerifiableElements = totalVerifiableElements;
    }

    public int getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(int totalFindings) {
        this.totalFindings = totalFindings;
    }

    public double getWeightedSum() {
        return weightedSum;
    }

    public void setWeightedSum(double weightedSum) {
        this.weightedSum = weightedSum;
    }

    public Map<String, Double> getCategoryScores() {
        return categoryScores;
    }

    public void setCategoryScores(Map<String, Double> categoryScores) {
        this.categoryScores = categoryScores;
    }

    public List<FindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<FindingDto> findings) {
        this.findings = findings;
    }
}
