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

class CodeAnalyzerTest {

    private CodeAnalyzer codeAnalyzer;

    @BeforeEach
    void setUp() {
        codeAnalyzer = new CodeAnalyzer();
    }

    @Test
    void testAnalyzeProjectCode_ExtractsEndpointsAndParams(@TempDir Path tempDir) throws Exception {
        String javaCode = """
            package com.example;
            import org.springframework.web.bind.annotation.*;

            @RestController
            @RequestMapping("/api/v1/users")
            public class UserController {

                @PostMapping
                public String createUser(@RequestParam String name, @RequestParam String email) {
                    return "ok";
                }
            }
            """;

        Path packageDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(packageDir);
        Files.writeString(packageDir.resolve("UserController.java"), javaCode);

        List<ExtractedElement> elements = codeAnalyzer.analyzeProjectCode(tempDir.toString());

        assertNotNull(elements);
        assertFalse(elements.isEmpty());

        boolean hasEndpoint = elements.stream()
                .anyMatch(e -> e.getType() == ElementType.ENDPOINT && e.getName().equals("/api/v1/users") && e.getValue().equals("POST"));
        assertTrue(hasEndpoint, "Should extract Spring REST endpoint");

        boolean hasNameParam = elements.stream()
                .anyMatch(e -> e.getType() == ElementType.PARAMETER && e.getName().equals("name"));
        assertTrue(hasNameParam, "Should extract method parameter 'name'");
    }
}
