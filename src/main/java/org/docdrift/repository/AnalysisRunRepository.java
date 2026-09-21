package org.docdrift.repository;

import org.docdrift.model.entity.AnalysisRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRunEntity, Long> {
    List<AnalysisRunEntity> findByProjectIdOrderByStartedAtDesc(Long projectId);
    Optional<AnalysisRunEntity> findFirstByProjectIdOrderByStartedAtDesc(Long projectId);
}
