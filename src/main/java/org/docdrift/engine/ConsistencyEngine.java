package org.docdrift.engine;

import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.dto.FindingDto;
import org.docdrift.model.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConsistencyEngine {

    private final SemanticEngine semanticEngine;

    @Autowired
    public ConsistencyEngine(SemanticEngine semanticEngine) {
        this.semanticEngine = semanticEngine;
    }

    public List<FindingDto> evaluateConsistency(List<ExtractedElement> codeElements,
                                               List<ExtractedElement> docElements,
                                               List<ExtractedElement> dbElements) {
        List<FindingDto> findings = new ArrayList<>();

        // Map elements by type
        Map<String, ExtractedElement> codeEndpoints = codeElements.stream()
                .filter(e -> e.getType() == ElementType.ENDPOINT)
                .collect(Collectors.toMap(
                        e -> e.getName() + ":" + e.getValue(), // e.g. /api/users:POST
                        e -> e,
                        (existing, replacement) -> existing
                ));

        Map<String, ExtractedElement> docEndpoints = docElements.stream()
                .filter(e -> e.getType() == ElementType.ENDPOINT)
                .collect(Collectors.toMap(
                        e -> e.getName() + ":" + e.getValue(),
                        e -> e,
                        (existing, replacement) -> existing
                ));

        // 1. Detect DC-02: Removed API & DC-05: HTTP Method Mismatch
        for (Map.Entry<String, ExtractedElement> docEpEntry : docEndpoints.entrySet()) {
            String epKey = docEpEntry.getKey();
            ExtractedElement docEp = docEpEntry.getValue();

            if (!codeEndpoints.containsKey(epKey)) {
                Optional<ExtractedElement> samePathCodeEp = codeEndpoints.values().stream()
                        .filter(e -> e.getName().equalsIgnoreCase(docEp.getName()))
                        .findFirst();

                if (samePathCodeEp.isPresent()) {
                    ExtractedElement codeEp = samePathCodeEp.get();
                    FindingDto f = createFinding(
                            Category.DC_05,
                            Severity.HIGH,
                            docEp.getSourceFile() + ":" + docEp.getLineNumber(),
                            codeEp.getSourceFile() + ":" + codeEp.getLineNumber(),
                            docEp.getValue() + " " + docEp.getName(),
                            codeEp.getValue() + " " + codeEp.getName(),
                            "Method mismatch for " + docEp.getName(),
                            "Update documentation to specify HTTP method " + codeEp.getValue()
                    );
                    findings.add(f);
                } else {
                    FindingDto f = createFinding(
                            Category.DC_02,
                            Severity.HIGH,
                            docEp.getSourceFile() + ":" + docEp.getLineNumber(),
                            "N/A",
                            docEp.getValue() + " " + docEp.getName() + " documented",
                            "Endpoint not found in source code",
                            "- " + docEp.getName(),
                            "Remove endpoint " + docEp.getName() + " from documentation or implement endpoint"
                    );
                    findings.add(f);
                }
            }
        }

        // 2. Detect DC-03: Undocumented API
        for (Map.Entry<String, ExtractedElement> codeEpEntry : codeEndpoints.entrySet()) {
            String epKey = codeEpEntry.getKey();
            ExtractedElement codeEp = codeEpEntry.getValue();

            if (!docEndpoints.containsKey(epKey) && docEndpoints.values().stream().noneMatch(d -> d.getName().equalsIgnoreCase(codeEp.getName()))) {
                FindingDto f = createFinding(
                        Category.DC_03,
                        Severity.MEDIUM,
                        "N/A",
                        codeEp.getSourceFile() + ":" + codeEp.getLineNumber(),
                        "No documentation found",
                        codeEp.getValue() + " " + codeEp.getName() + " exists in code",
                        "+ " + codeEp.getName(),
                        "Add endpoint documentation for " + codeEp.getValue() + " " + codeEp.getName()
                );
                findings.add(f);
            }
        }

        // 3. Detect DC-01: Parameter Mismatch
        for (ExtractedElement codeEp : codeEndpoints.values()) {
            List<String> codeParams = codeElements.stream()
                    .filter(e -> e.getType() == ElementType.PARAMETER && codeEp.getName().equals(e.getParentName()))
                    .map(ExtractedElement::getName)
                    .distinct()
                    .toList();

            List<String> docParams = docElements.stream()
                    .filter(e -> e.getType() == ElementType.PARAMETER)
                    .map(ExtractedElement::getName)
                    .distinct()
                    .toList();

            if (!codeParams.isEmpty() && !docParams.isEmpty()) {
                List<String> missingFromDocs = codeParams.stream()
                        .filter(cp -> docParams.stream().noneMatch(dp -> dp.equalsIgnoreCase(cp) || semanticEngine.calculateSimilarity(cp, dp) > 0.8))
                        .toList();

                if (!missingFromDocs.isEmpty()) {
                    FindingDto f = createFinding(
                            Category.DC_01,
                            Severity.MEDIUM,
                            "Documentation file",
                            codeEp.getSourceFile() + ":" + codeEp.getLineNumber(),
                            "Documented: " + String.join(", ", docParams),
                            "Implemented: " + String.join(", ", codeParams),
                            "Missing from docs: + " + String.join(", ", missingFromDocs),
                            "Add missing parameters (" + String.join(", ", missingFromDocs) + ") to endpoint request schema."
                    );
                    findings.add(f);
                }
            }
        }

        // 4. Detect DC-08: README Version / Dependency Drift
        Optional<ExtractedElement> codeJavaVer = codeElements.stream()
                .filter(e -> e.getType() == ElementType.JAVA_VERSION)
                .findFirst();

        Optional<ExtractedElement> docJavaVer = docElements.stream()
                .filter(e -> e.getType() == ElementType.JAVA_VERSION)
                .findFirst();

        if (codeJavaVer.isPresent() && docJavaVer.isPresent()) {
            String cVer = codeJavaVer.get().getValue();
            String dVer = docJavaVer.get().getValue();
            if (!cVer.equalsIgnoreCase(dVer)) {
                FindingDto f = createFinding(
                        Category.DC_08,
                        Severity.MEDIUM,
                        docJavaVer.get().getSourceFile() + ":" + docJavaVer.get().getLineNumber(),
                        codeJavaVer.get().getSourceFile() + ":" + codeJavaVer.get().getLineNumber(),
                        "Java " + dVer,
                        "Java " + cVer,
                        "Version mismatch: Java " + dVer + " vs Java " + cVer,
                        "Update README to state Java " + cVer
                );
                findings.add(f);
            }
        }

        // 5. Detect DC-09: Configuration Mismatch
        List<ExtractedElement> codeConfigs = codeElements.stream()
                .filter(e -> e.getType() == ElementType.CONFIG_KEY)
                .toList();

        List<ExtractedElement> docConfigs = docElements.stream()
                .filter(e -> e.getType() == ElementType.CONFIG_KEY)
                .toList();

        for (ExtractedElement dCfg : docConfigs) {
            Optional<ExtractedElement> cCfgOpt = codeConfigs.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(dCfg.getName()) || c.getName().endsWith(dCfg.getName()))
                    .findFirst();

            if (cCfgOpt.isPresent()) {
                ExtractedElement cCfg = cCfgOpt.get();
                if (!dCfg.getValue().equalsIgnoreCase(cCfg.getValue())) {
                    FindingDto f = createFinding(
                            Category.DC_09,
                            Severity.MEDIUM,
                            dCfg.getSourceFile() + ":" + dCfg.getLineNumber(),
                            cCfg.getSourceFile() + ":" + cCfg.getLineNumber(),
                            dCfg.getName() + "=" + dCfg.getValue(),
                            cCfg.getName() + "=" + cCfg.getValue(),
                            "Config value mismatch: " + dCfg.getValue() + " vs " + cCfg.getValue(),
                            "Update documentation to set " + dCfg.getName() + "=" + cCfg.getValue()
                    );
                    findings.add(f);
                }
            }
        }

        // 6. Detect DC-06: Database Drift
        List<ExtractedElement> dbCols = dbElements.stream()
                .filter(e -> e.getType() == ElementType.DB_COLUMN)
                .toList();

        List<ExtractedElement> docCols = docElements.stream()
                .filter(e -> e.getType() == ElementType.DB_COLUMN)
                .toList();

        if (!dbCols.isEmpty()) {
            for (ExtractedElement dbCol : dbCols) {
                boolean isDoc = docCols.stream().anyMatch(d -> d.getName().equalsIgnoreCase(dbCol.getName()));
                if (!isDoc && !docCols.isEmpty()) {
                    FindingDto f = createFinding(
                            Category.DC_06,
                            Severity.MEDIUM,
                            "Database Documentation",
                            dbCol.getSourceFile() + ":" + dbCol.getLineNumber(),
                            "Column " + dbCol.getName() + " missing from docs",
                            "Column " + dbCol.getName() + " (" + dbCol.getValue() + ") in table " + dbCol.getParentName(),
                            "+ " + dbCol.getName(),
                            "Document column " + dbCol.getName() + " in database documentation schema."
                    );
                    findings.add(f);
                }
            }
        }

        // 7. Detect DC-07: Javadoc Mismatch
        List<ExtractedElement> javadocTags = codeElements.stream()
                .filter(e -> e.getType() == ElementType.JAVADOC_TAG)
                .toList();

        for (ExtractedElement jtag : javadocTags) {
            String methodName = jtag.getParentName();
            if ("param".equals(jtag.getMetadata().get("tagType"))) {
                boolean paramExistsInMethod = codeElements.stream()
                        .filter(e -> e.getType() == ElementType.PARAMETER)
                        .anyMatch(p -> p.getName().equals(jtag.getName()));

                if (!paramExistsInMethod) {
                    FindingDto f = createFinding(
                            Category.DC_07,
                            Severity.MEDIUM,
                            jtag.getSourceFile() + ":" + jtag.getLineNumber(),
                            jtag.getSourceFile() + ":" + jtag.getLineNumber(),
                            "@param " + jtag.getName(),
                            "Parameter " + jtag.getName() + " not in method signature",
                            "- @param " + jtag.getName(),
                            "Remove outdated @param " + jtag.getName() + " tag from Javadoc"
                    );
                    findings.add(f);
                }
            }
        }

        return findings;
    }

    private FindingDto createFinding(Category category, Severity severity,
                                      String docLocation, String codeLocation,
                                      String docVal, String actualVal,
                                      String diff, String suggestion) {
        FindingDto f = new FindingDto();
        f.setCategory(category);
        f.setSeverity(severity);
        f.setDocLocation(docLocation);
        f.setCodeLocation(codeLocation);
        f.setDocumentedValue(docVal);
        f.setActualValue(actualVal);
        f.setDifference(diff);
        f.setSuggestion(suggestion);
        f.setConfidence(0.95);
        f.setMatchMethod(MatchMethod.HYBRID);
        f.setStatus(FindingStatus.OPEN);
        f.setFirstSeenVersion("v1.0.0");
        return f;
    }
}
