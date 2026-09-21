package org.docdrift.controller;

import jakarta.validation.Valid;
import org.docdrift.model.dto.AnalysisResponse;
import org.docdrift.model.dto.ProjectRegistrationRequest;
import org.docdrift.model.entity.ProjectEntity;
import org.docdrift.service.AnalysisService;
import org.docdrift.service.IngestionService;
import org.docdrift.service.ReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private final IngestionService ingestionService;
    private final AnalysisService analysisService;
    private final ReportExportService reportExportService;

    @Autowired
    public ProjectController(IngestionService ingestionService,
                             AnalysisService analysisService,
                             ReportExportService reportExportService) {
        this.ingestionService = ingestionService;
        this.analysisService = analysisService;
        this.reportExportService = reportExportService;
    }

    @PostMapping("/register")
    public ResponseEntity<ProjectEntity> registerProject(@Valid @RequestBody ProjectRegistrationRequest request) {
        ProjectEntity project = ingestionService.registerProject(
                request.getName(),
                request.getRootPath(),
                request.getDescription()
        );
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectEntity>> listProjects() {
        return ResponseEntity.ok(ingestionService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectEntity> getProject(@PathVariable Long id) {
        return ingestionService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<AnalysisResponse> analyzeProject(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "v1.0.0") String version) {
        AnalysisResponse response = analysisService.runAnalysis(id, version);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/runs/latest")
    public ResponseEntity<AnalysisResponse> getLatestRun(@PathVariable Long id) {
        return analysisService.getLatestRun(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/export/markdown")
    public ResponseEntity<String> exportMarkdownReport(@PathVariable Long id) {
        return analysisService.getLatestRun(id)
                .map(analysis -> {
                    String md = reportExportService.generateMarkdownReport(analysis);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"docdrift-report.md\"")
                            .contentType(MediaType.TEXT_MARKDOWN)
                            .body(md);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
