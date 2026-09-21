package org.docdrift.controller;

import org.docdrift.model.entity.FindingEntity;
import org.docdrift.model.enums.FindingStatus;
import org.docdrift.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/findings")
@CrossOrigin(origins = "*")
public class FindingController {

    private final AnalysisService analysisService;

    @Autowired
    public FindingController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<FindingEntity> updateStatus(
            @PathVariable Long id,
            @RequestParam FindingStatus status) {
        return analysisService.updateFindingStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
