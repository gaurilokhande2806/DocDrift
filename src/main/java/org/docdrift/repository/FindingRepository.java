package org.docdrift.repository;

import org.docdrift.model.entity.FindingEntity;
import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.FindingStatus;
import org.docdrift.model.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindingRepository extends JpaRepository<FindingEntity, Long> {
    List<FindingEntity> findByAnalysisRunId(Long analysisRunId);
    List<FindingEntity> findByAnalysisRunIdAndStatus(Long analysisRunId, FindingStatus status);
    List<FindingEntity> findByAnalysisRunIdAndCategory(Long analysisRunId, Category category);
    List<FindingEntity> findByAnalysisRunIdAndSeverity(Long analysisRunId, Severity severity);
}
