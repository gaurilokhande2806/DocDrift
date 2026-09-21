package org.docdrift.engine;

import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.dto.FindingDto;
import org.docdrift.model.enums.Category;
import org.docdrift.model.enums.ElementType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsistencyEngineTest {

    private ConsistencyEngine consistencyEngine;

    @BeforeEach
    void setUp() {
        SemanticEngine semanticEngine = new SemanticEngine();
        consistencyEngine = new ConsistencyEngine(semanticEngine);
    }

    @Test
    void testEvaluateConsistency_DetectsRemovedApi() {
        List<ExtractedElement> codeElements = new ArrayList<>();
        List<ExtractedElement> docElements = new ArrayList<>();
        List<ExtractedElement> dbElements = new ArrayList<>();

        // Documented endpoint that doesn't exist in code
        ExtractedElement docEp = new ExtractedElement(ElementType.ENDPOINT, "README.md", 10, "/api/products", "GET");
        docElements.add(docEp);

        List<FindingDto> findings = consistencyEngine.evaluateConsistency(codeElements, docElements, dbElements);

        assertNotNull(findings);
        assertFalse(findings.isEmpty());
        boolean hasRemovedApi = findings.stream().anyMatch(f -> f.getCategory() == Category.DC_02);
        assertTrue(hasRemovedApi, "Should detect DC-02 Removed API finding");
    }

    @Test
    void testEvaluateConsistency_DetectsUndocumentedApi() {
        List<ExtractedElement> codeElements = new ArrayList<>();
        List<ExtractedElement> docElements = new ArrayList<>();
        List<ExtractedElement> dbElements = new ArrayList<>();

        // Code endpoint missing in docs
        ExtractedElement codeEp = new ExtractedElement(ElementType.ENDPOINT, "PaymentController.java", 15, "/api/payments", "POST");
        codeElements.add(codeEp);

        List<FindingDto> findings = consistencyEngine.evaluateConsistency(codeElements, docElements, dbElements);

        assertNotNull(findings);
        assertFalse(findings.isEmpty());
        boolean hasUndocumentedApi = findings.stream().anyMatch(f -> f.getCategory() == Category.DC_03);
        assertTrue(hasUndocumentedApi, "Should detect DC-03 Undocumented API finding");
    }
}
