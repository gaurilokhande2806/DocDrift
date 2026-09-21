package org.docdrift.service;

import org.docdrift.model.entity.ProjectEntity;
import org.docdrift.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class IngestionService {

    private final ProjectRepository projectRepository;

    @Autowired
    public IngestionService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectEntity registerProject(String name, String rootPath, String description) {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Invalid project directory path: " + rootPath);
        }

        Optional<ProjectEntity> existing = projectRepository.findByName(name);
        if (existing.isPresent()) {
            ProjectEntity project = existing.get();
            project.setRootPath(rootPath);
            project.setDescription(description);
            return projectRepository.save(project);
        }

        ProjectEntity project = new ProjectEntity(name, rootPath, description);
        return projectRepository.save(project);
    }

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<ProjectEntity> getProjectById(Long id) {
        return projectRepository.findById(id);
    }
}
