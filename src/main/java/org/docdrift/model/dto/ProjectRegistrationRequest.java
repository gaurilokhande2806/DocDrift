package org.docdrift.model.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectRegistrationRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    @NotBlank(message = "Project root path is required")
    private String rootPath;

    private String description;

    public ProjectRegistrationRequest() {
    }

    public ProjectRegistrationRequest(String name, String rootPath, String description) {
        this.name = name;
        this.rootPath = rootPath;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
