package org.docdrift.model.enums;

public enum Severity {
    LOW(1.0, "Cosmetic; does not mislead behaviour."),
    MEDIUM(2.0, "Incomplete information; users can still succeed."),
    HIGH(4.0, "Following the docs will likely cause failure."),
    CRITICAL(8.0, "Documentation is dangerously misleading, including destructive operations.");

    private final double weight;
    private final String description;

    Severity(double weight, String description) {
        this.weight = weight;
        this.description = description;
    }

    public double getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }
}
