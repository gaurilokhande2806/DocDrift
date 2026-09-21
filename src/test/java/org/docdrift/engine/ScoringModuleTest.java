package org.docdrift.engine;

import org.docdrift.model.dto.FindingDto;
import org.docdrift.model.dto.HealthScoreReport;
import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.DecayLevel;
import org.docdrift.model.enums.Severity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoringModuleTest {

    private ScoringModule scoringModule;

    @BeforeEach
    void setUp() {
        scoringModule = new ScoringModule();
    }

    @Test
    void testComputeHealthScore_MatchesWorkedExampleInPRD() {
        // Worked example from PRD Section 9:
        // 50 verifiable elements with 2 critical (8), 4 high (4), 6 medium (2) and 5 low (1) findings
        // Weighted sum = 2*8 + 4*4 + 6*2 + 5*1 = 16 + 16 + 12 + 5 = 49
        // DHS = 100 * (1 - 49 / (8 * 50)) = 87.75

        List<FindingDto> findings = new ArrayList<>();
        addFindings(findings, Category.DC_02, Severity.CRITICAL, 2);
        addFindings(findings, Category.DC_05, Severity.HIGH, 4);
        addFindings(findings, Category.DC_01, Severity.MEDIUM, 6);
        addFindings(findings, Category.DC_10, Severity.LOW, 5);

        HealthScoreReport report = scoringModule.computeHealthScore(50, findings);

        assertEquals(87.75, report.getOverallDhs(), 0.01, "DHS score should equal 87.75 according to PRD formula");
        assertEquals(DecayLevel.MILD, report.getDecayLevel(), "Score 87.75 should be classified as MILD decay");
    }

    private void addFindings(List<FindingDto> findings, Category category, Severity severity, int count) {
        for (int i = 0; i < count; i++) {
            FindingDto f = new FindingDto();
            f.setCategory(category);
            f.setSeverity(severity);
            findings.add(f);
        }
    }
}
