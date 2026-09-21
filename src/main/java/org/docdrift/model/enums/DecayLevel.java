package org.docdrift.model.enums;

public enum DecayLevel {
    HEALTHY(90.0, 100.0, "Healthy documentation consistency"),
    MILD(75.0, 89.99, "Mild documentation decay"),
    MODERATE(50.0, 74.99, "Moderate documentation decay"),
    SEVERE(0.0, 49.99, "Severe documentation decay");

    private final double minScore;
    private final double maxScore;
    private final String label;

    DecayLevel(double minScore, double maxScore, String label) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.label = label;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String getLabel() {
        return label;
    }

    public static DecayLevel fromScore(double score) {
        if (score >= 90.0) return HEALTHY;
        if (score >= 75.0) return MILD;
        if (score >= 50.0) return MODERATE;
        return SEVERE;
    }
}
