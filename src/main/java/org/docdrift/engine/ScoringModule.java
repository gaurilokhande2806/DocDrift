package org.docdrift.engine;

import org.docdrift.model.dto.FindingDto;
import org.docdrift.model.dto.HealthScoreReport;
import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.DecayLevel;
import org.docdrift.model.enums.FindingStatus;
import org.docdrift.model.enums.Severity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScoringModule {

    @Value("${docdrift.scoring.max-weight:8.0}")
    private double maxWeight = 8.0;

    public HealthScoreReport computeHealthScore(int totalVerifiableElements, List<FindingDto> findings) {
        // Exclude FALSE_POSITIVE and ACCEPTED findings from scoring
        List<FindingDto> activeFindings = findings.stream()
                .filter(f -> f.getStatus() == FindingStatus.OPEN || f.getStatus() == null)
                .toList();

        int totalActiveFindings = activeFindings.size();
        int verifiableElements = Math.max(totalVerifiableElements, totalActiveFindings);

        if (verifiableElements == 0) {
            HealthScoreReport report = new HealthScoreReport(100.0, DecayLevel.HEALTHY, 0, 0, 0.0);
            report.setFindings(findings);
            return report;
        }

        double weightedSum = 0.0;
        Map<String, Double> categoryWeightedSums = new HashMap<>();

        for (FindingDto f : activeFindings) {
            double weight = f.getSeverity() != null ? f.getSeverity().getWeight() : Severity.MEDIUM.getWeight();
            weightedSum += weight;

            String categoryCode = f.getCategory() != null ? f.getCategory().getCode() : "OTHER";
            categoryWeightedSums.put(categoryCode, categoryWeightedSums.getOrDefault(categoryCode, 0.0) + weight);
        }

        // Formula: DHS = 100 * (1 - (sum of w(f) / (w_max * N)))
        double dhs = 100.0 * (1.0 - (weightedSum / (maxWeight * verifiableElements)));
        dhs = Math.max(0.0, Math.min(100.0, dhs));

        // Format to 2 decimal places
        dhs = Math.round(dhs * 100.0) / 100.0;
        DecayLevel decayLevel = DecayLevel.fromScore(dhs);

        HealthScoreReport report = new HealthScoreReport(
                dhs,
                decayLevel,
                verifiableElements,
                totalActiveFindings,
                weightedSum
        );

        // Compute sub-scores per domain category (API, DB, README, Javadoc, Config)
        Map<String, Double> categoryScores = new HashMap<>();
        categoryScores.put("API Consistency", calculateSubScore(activeFindings, Category.DC_01, Category.DC_02, Category.DC_03, Category.DC_04, Category.DC_05));
        categoryScores.put("Database Consistency", calculateSubScore(activeFindings, Category.DC_06));
        categoryScores.put("Javadoc Consistency", calculateSubScore(activeFindings, Category.DC_07));
        categoryScores.put("README & Build Consistency", calculateSubScore(activeFindings, Category.DC_08));
        categoryScores.put("Configuration Consistency", calculateSubScore(activeFindings, Category.DC_09));

        report.setCategoryScores(categoryScores);
        report.setFindings(findings);

        return report;
    }

    private double calculateSubScore(List<FindingDto> findings, Category... categories) {
        List<Category> catList = List.of(categories);
        long count = findings.stream().filter(f -> catList.contains(f.getCategory())).count();
        if (count == 0) return 100.0;
        double catWeightedSum = findings.stream()
                .filter(f -> catList.contains(f.getCategory()))
                .mapToDouble(f -> f.getSeverity().getWeight())
                .sum();
        double subDhs = 100.0 * (1.0 - (catWeightedSum / (maxWeight * (count + 5))));
        return Math.round(Math.max(0.0, subDhs) * 100.0) / 100.0;
    }
}
