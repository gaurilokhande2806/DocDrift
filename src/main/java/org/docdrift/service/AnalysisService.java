package org.docdrift.service;

import org.docdrift.analyzer.CodeAnalyzer;
import org.docdrift.analyzer.DbAnalyzer;
import org.docdrift.analyzer.DocAnalyzer;
import org.docdrift.engine.ConsistencyEngine;
import org.docdrift.engine.ScoringModule;
import org.docdrift.model.dto.AnalysisResponse;
import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.dto.FindingDto;
import org.docdrift.model.dto.HealthScoreReport;
import org.docdrift.model.entity.AnalysisRunEntity;
import org.docdrift.model.entity.FindingEntity;
import org.docdrift.model.entity.ProjectEntity;
import org.docdrift.model.enums.FindingStatus;

import org.docdrift.repository.AnalysisRunRepository;
import org.docdrift.repository.FindingRepository;
import org.docdrift.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final FindingRepository findingRepository;
    private final CodeAnalyzer codeAnalyzer;
    private final DocAnalyzer docAnalyzer;
    private final DbAnalyzer dbAnalyzer;
    private final ConsistencyEngine consistencyEngine;
    private final ScoringModule scoringModule;

    @Autowired
    public AnalysisService(ProjectRepository projectRepository,
                           AnalysisRunRepository analysisRunRepository,
                           FindingRepository findingRepository,
                           CodeAnalyzer codeAnalyzer,
                           DocAnalyzer docAnalyzer,
                           DbAnalyzer dbAnalyzer,
                           ConsistencyEngine consistencyEngine,
                           ScoringModule scoringModule) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.findingRepository = findingRepository;
        this.codeAnalyzer = codeAnalyzer;
        this.docAnalyzer = docAnalyzer;
        this.dbAnalyzer = dbAnalyzer;
        this.consistencyEngine = consistencyEngine;
        this.scoringModule = scoringModule;
    }

    @Transactional
    public AnalysisResponse runAnalysis(Long projectId, String version) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        AnalysisRunEntity run = new AnalysisRunEntity(project, version != null ? version : "v1.0.0");
        run = analysisRunRepository.save(run);

        try {
            // 1. Extract Elements
            List<ExtractedElement> codeElements = codeAnalyzer.analyzeProjectCode(project.getRootPath());
            List<ExtractedElement> docElements = docAnalyzer.analyzeProjectDocumentation(project.getRootPath());
            List<ExtractedElement> dbElements = dbAnalyzer.analyzeProjectDatabaseSchema(project.getRootPath());

            int totalVerifiableElements = codeElements.size() + docElements.size() + dbElements.size();

            // 2. Consistency Analysis
            List<FindingDto> findingDtos = consistencyEngine.evaluateConsistency(codeElements, docElements, dbElements);

            // 3. Compute Scoring & DHS
            HealthScoreReport scoreReport = scoringModule.computeHealthScore(totalVerifiableElements, findingDtos);

            // 4. Update Analysis Run Record
            run.setOverallDhs(scoreReport.getOverallDhs());
            run.setDecayLevel(scoreReport.getDecayLevel());
            run.setTotalVerifiableElements(scoreReport.getTotalVerifiableElements());
            run.setTotalFindings(scoreReport.getTotalFindings());
            run.setApiConsistencyScore(scoreReport.getCategoryScores().getOrDefault("API Consistency", 100.0));
            run.setDbConsistencyScore(scoreReport.getCategoryScores().getOrDefault("Database Consistency", 100.0));
            run.setReadmeConsistencyScore(scoreReport.getCategoryScores().getOrDefault("README & Build Consistency", 100.0));
            run.setJavadocConsistencyScore(scoreReport.getCategoryScores().getOrDefault("Javadoc Consistency", 100.0));
            run.setFinishedAt(LocalDateTime.now());
            run.setStatus("COMPLETED");

            analysisRunRepository.save(run);

            // 5. Save Findings to DB
            List<FindingDto> savedFindingDtos = new ArrayList<>();
            for (FindingDto dto : findingDtos) {
                FindingEntity entity = new FindingEntity();
                entity.setAnalysisRun(run);
                entity.setCategory(dto.getCategory());
                entity.setSeverity(dto.getSeverity());
                entity.setDocLocation(dto.getDocLocation());
                entity.setCodeLocation(dto.getCodeLocation());
                entity.setDocumentedValue(dto.getDocumentedValue());
                entity.setActualValue(dto.getActualValue());
                entity.setDifference(dto.getDifference());
                entity.setSuggestion(dto.getSuggestion());
                entity.setConfidence(dto.getConfidence());
                entity.setMatchMethod(dto.getMatchMethod());
                entity.setStatus(dto.getStatus() != null ? dto.getStatus() : FindingStatus.OPEN);
                entity.setFirstSeenVersion(run.getVersion());

                FindingEntity savedEntity = findingRepository.save(entity);
                dto.setId(savedEntity.getId());
                savedFindingDtos.add(dto);
            }

            return mapToResponse(run, savedFindingDtos);

        } catch (Exception e) {
            run.setStatus("FAILED");
            run.setFinishedAt(LocalDateTime.now());
            analysisRunRepository.save(run);
            throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
        }
    }

    public Optional<AnalysisResponse> getLatestRun(Long projectId) {
        return analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .map(run -> {
                    List<FindingEntity> entities = findingRepository.findByAnalysisRunId(run.getId());
                    List<FindingDto> dtos = entities.stream().map(this::mapToFindingDto).toList();
                    return mapToResponse(run, dtos);
                });
    }

    public Optional<FindingEntity> updateFindingStatus(Long findingId, FindingStatus newStatus) {
        return findingRepository.findById(findingId).map(finding -> {
            finding.setStatus(newStatus);
            return findingRepository.save(finding);
        });
    }

    private AnalysisResponse mapToResponse(AnalysisRunEntity run, List<FindingDto> findings) {
        AnalysisResponse response = new AnalysisResponse();
        response.setRunId(run.getId());
        response.setProjectId(run.getProject().getId());
        response.setProjectName(run.getProject().getName());
        response.setVersion(run.getVersion());
        response.setOverallDhs(run.getOverallDhs());
        response.setDecayLevel(run.getDecayLevel());
        response.setTotalVerifiableElements(run.getTotalVerifiableElements());
        response.setTotalFindings(run.getTotalFindings());
        response.setStartedAt(run.getStartedAt());
        response.setFinishedAt(run.getFinishedAt());
        response.setStatus(run.getStatus());
        response.setFindings(findings);
        return response;
    }

    private FindingDto mapToFindingDto(FindingEntity entity) {
        FindingDto dto = new FindingDto();
        dto.setId(entity.getId());
        dto.setCategory(entity.getCategory());
        dto.setSeverity(entity.getSeverity());
        dto.setDocLocation(entity.getDocLocation());
        dto.setCodeLocation(entity.getCodeLocation());
        dto.setDocumentedValue(entity.getDocumentedValue());
        dto.setActualValue(entity.getActualValue());
        dto.setDifference(entity.getDifference());
        dto.setSuggestion(entity.getSuggestion());
        dto.setConfidence(entity.getConfidence());
        dto.setMatchMethod(entity.getMatchMethod());
        dto.setStatus(entity.getStatus());
        dto.setFirstSeenVersion(entity.getFirstSeenVersion());
        return dto;
    }
}
