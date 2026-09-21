package org.docdrift.service;

import org.docdrift.model.dto.AnalysisResponse;
import org.docdrift.model.dto.FindingDto;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    public String generateMarkdownReport(AnalysisResponse analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("# DocDrift Report: ").append(analysis.getProjectName()).append("\n\n");
        sb.append("**DOCUMENTATION HEALTH SCORE**: ").append(analysis.getOverallDhs()).append(" / 100 ");
        sb.append("(").append(analysis.getDecayLevel() != null ? analysis.getDecayLevel().getLabel() : "Unknown").append(")\n\n");

        sb.append("## Analysis Summary\n");
        sb.append("- **Project Name**: ").append(analysis.getProjectName()).append("\n");
        sb.append("- **Version**: ").append(analysis.getVersion()).append("\n");
        sb.append("- **Total Verifiable Elements**: ").append(analysis.getTotalVerifiableElements()).append("\n");
        sb.append("- **Total Inconsistencies Detected**: ").append(analysis.getTotalFindings()).append("\n");
        sb.append("- **Analysis Date**: ").append(analysis.getFinishedAt() != null ? analysis.getFinishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A").append("\n\n");

        sb.append("## Findings Breakdown\n\n");

        List<FindingDto> findings = analysis.getFindings();
        if (findings == null || findings.isEmpty()) {
            sb.append("No documentation inconsistency issues detected!\n");
        } else {
            for (int i = 0; i < findings.size(); i++) {
                FindingDto f = findings.get(i);
                sb.append("### Finding #").append(i + 1).append(" | ")
                        .append(f.getCategory() != null ? f.getCategory().getCode() + " " + f.getCategory().getTitle() : "Unknown")
                        .append(" | ").append(f.getSeverity()).append("\n");
                sb.append("- **Doc Location**: `").append(f.getDocLocation()).append("`\n");
                sb.append("- **Code Location**: `").append(f.getCodeLocation()).append("`\n");
                sb.append("- **Documented Value**: ").append(f.getDocumentedValue()).append("\n");
                sb.append("- **Actual Value**: ").append(f.getActualValue()).append("\n");
                sb.append("- **Difference**: ").append(f.getDifference()).append("\n");
                sb.append("- **First Seen In**: ").append(f.getFirstSeenVersion()).append("\n");
                sb.append("- **Suggestion**: ").append(f.getSuggestion()).append("\n\n");
            }
        }

        return sb.toString();
    }
}
