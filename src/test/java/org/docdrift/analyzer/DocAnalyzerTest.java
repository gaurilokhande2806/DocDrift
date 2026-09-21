package org.docdrift.analyzer;

import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.enums.ElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocAnalyzerTest {

    private DocAnalyzer docAnalyzer;

    @BeforeEach
    void setUp() {
        docAnalyzer = new DocAnalyzer();
    }

    @Test
    void testAnalyzeProjectDocumentation_ExtractsMarkdownEndpoints(@TempDir Path tempDir) throws Exception {
        String mdContent = """
            # API Documentation
            Prerequisites: Java 17
            
            - **POST /api/users**: Register user
              Accepts parameters: name, email
            """;

        Files.writeString(tempDir.resolve("README.md"), mdContent);

        List<ExtractedElement> elements = docAnalyzer.analyzeProjectDocumentation(tempDir.toString());

        assertNotNull(elements);
        assertFalse(elements.isEmpty());

        boolean hasEndpoint = elements.stream()
                .anyMatch(e -> e.getType() == ElementType.ENDPOINT && e.getName().equals("/api/users") && e.getValue().equals("POST"));
        assertTrue(hasEndpoint, "Should extract documented POST /api/users endpoint");

        boolean hasJavaVer = elements.stream()
                .anyMatch(e -> e.getType() == ElementType.JAVA_VERSION && e.getValue().equals("17"));
        assertTrue(hasJavaVer, "Should extract documented Java version");
    }
}
