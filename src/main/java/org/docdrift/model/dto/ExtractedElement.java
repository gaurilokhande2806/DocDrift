package org.docdrift.model.dto;

import org.docdrift.model.enums.ElementType;

import java.util.HashMap;
import java.util.Map;

public class ExtractedElement {
    private String id;
    private ElementType type;
    private String sourceFile;
    private int lineNumber;
    private String name;
    private String value;
    private String parentName; // e.g. endpoint path for parameters, table name for columns
    private Map<String, String> metadata = new HashMap<>();

    public ExtractedElement() {
    }

    public ExtractedElement(ElementType type, String sourceFile, int lineNumber, String name, String value) {
        this.type = type;
        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
