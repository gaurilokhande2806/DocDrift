package org.docdrift.model.enums;

public enum Category {
    DC_01("DC-01", "Parameter Mismatch", Severity.MEDIUM, "Documented and implemented parameters differ."),
    DC_02("DC-02", "Removed API", Severity.HIGH, "Documented endpoint has no implementation."),
    DC_03("DC-03", "Undocumented API", Severity.MEDIUM, "Implemented endpoint has no documentation."),
    DC_04("DC-04", "Response Mismatch", Severity.MEDIUM, "Documented response fields differ from actual DTO return type."),
    DC_05("DC-05", "HTTP Method Mismatch", Severity.HIGH, "Documented HTTP method differs from implemented method."),
    DC_06("DC-06", "Database Drift", Severity.MEDIUM, "Documented schema differs from actual SQL DDL schema."),
    DC_07("DC-07", "Javadoc Mismatch", Severity.MEDIUM, "Javadoc tags do not match the method signature parameters or return type."),
    DC_08("DC-08", "README Version/Dependency Drift", Severity.MEDIUM, "Documented Java version or dependency differs from pom.xml/build.gradle."),
    DC_09("DC-09", "Configuration Mismatch", Severity.MEDIUM, "Documented configuration values/keys differ from actual application configuration."),
    DC_10("DC-10", "Spelling or Terminology Error", Severity.LOW, "Misspelt technical terms or inconsistent naming in documentation.");

    private final String code;
    private final String title;
    private final Severity defaultSeverity;
    private final String description;

    Category(String code, String title, Severity defaultSeverity, String description) {
        this.code = code;
        this.title = title;
        this.defaultSeverity = defaultSeverity;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public Severity getDefaultSeverity() {
        return defaultSeverity;
    }

    public String getDescription() {
        return description;
    }
}
