package org.docdrift.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.enums.ElementType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class CodeAnalyzer {

    public List<ExtractedElement> analyzeProjectCode(String projectPath) {
        List<ExtractedElement> elements = new ArrayList<>();
        Path root = Paths.get(projectPath);

        if (!Files.exists(root)) {
            return elements;
        }

        // 1. Parse Java Source Files
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> parseJavaFile(path, elements));
        } catch (Exception e) {
            // Log or record warning
        }

        // 2. Parse pom.xml for Java version & Dependencies
        Path pomPath = root.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            parsePomXml(pomPath, elements);
        }

        // 3. Parse application.properties / application.yml
        Path appProps = root.resolve("src/main/resources/application.properties");
        if (Files.exists(appProps)) {
            parseProperties(appProps, elements);
        }

        Path appYml = root.resolve("src/main/resources/application.yml");
        if (Files.exists(appYml)) {
            parseYml(appYml, elements);
        }

        return elements;
    }

    private void parseJavaFile(Path javaFilePath, List<ExtractedElement> elements) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFilePath);
            String relativePath = javaFilePath.getFileName().toString();

            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                String classPrefix = extractClassMappingPrefix(classDecl);

                // Extract DTO Fields if applicable
                boolean isDtoOrEntity = classDecl.getNameAsString().endsWith("Dto") ||
                        classDecl.getNameAsString().endsWith("Request") ||
                        classDecl.getNameAsString().endsWith("Response") ||
                        classDecl.getNameAsString().endsWith("Entity");

                if (isDtoOrEntity) {
                    for (FieldDeclaration field : classDecl.getFields()) {
                        field.getVariables().forEach(var -> {
                            ExtractedElement elem = new ExtractedElement(
                                    ElementType.RESPONSE_FIELD,
                                    relativePath,
                                    var.getBegin().map(p -> p.line).orElse(1),
                                    var.getNameAsString(),
                                    field.getElementType().asString()
                            );
                            elem.setParentName(classDecl.getNameAsString());
                            elements.add(elem);
                        });
                    }
                }

                // Process methods for REST Endpoints & Javadoc
                for (MethodDeclaration method : classDecl.getMethods()) {
                    int line = method.getBegin().map(p -> p.line).orElse(1);
                    extractRestEndpoint(method, classPrefix, relativePath, line, elements);
                    extractJavadocTags(method, relativePath, line, elements);
                }
            }
        } catch (Exception e) {
            // Skip unparseable files safely (NFR-05)
        }
    }

    private String extractClassMappingPrefix(ClassOrInterfaceDeclaration classDecl) {
        for (AnnotationExpr ann : classDecl.getAnnotations()) {
            if ("RequestMapping".equals(ann.getNameAsString())) {
                return extractAnnotationPath(ann);
            }
        }
        return "";
    }

    private void extractRestEndpoint(MethodDeclaration method, String classPrefix, String relativePath, int line, List<ExtractedElement> elements) {
        for (AnnotationExpr ann : method.getAnnotations()) {
            String annName = ann.getNameAsString();
            String httpMethod = null;

            if ("GetMapping".equals(annName)) httpMethod = "GET";
            else if ("PostMapping".equals(annName)) httpMethod = "POST";
            else if ("PutMapping".equals(annName)) httpMethod = "PUT";
            else if ("DeleteMapping".equals(annName)) httpMethod = "DELETE";
            else if ("PatchMapping".equals(annName)) httpMethod = "PATCH";
            else if ("RequestMapping".equals(annName)) {
                httpMethod = extractMethodFromRequestMapping(ann);
            }

            if (httpMethod != null) {
                String methodPath = extractAnnotationPath(ann);
                String fullPath = sanitizePath(classPrefix + methodPath);

                ExtractedElement endpointElem = new ExtractedElement(
                        ElementType.ENDPOINT,
                        relativePath,
                        line,
                        fullPath,
                        httpMethod
                );
                endpointElem.setParentName(method.getNameAsString());
                elements.add(endpointElem);

                // Extract Parameters
                for (Parameter param : method.getParameters()) {
                    String paramName = param.getNameAsString();
                    String paramType = param.getTypeAsString();

                    ExtractedElement paramElem = new ExtractedElement(
                            ElementType.PARAMETER,
                            relativePath,
                            param.getBegin().map(p -> p.line).orElse(line),
                            paramName,
                            paramType
                    );
                    paramElem.setParentName(fullPath);
                    paramElem.getMetadata().put("httpMethod", httpMethod);
                    elements.add(paramElem);
                }
            }
        }
    }

    private String extractAnnotationPath(AnnotationExpr ann) {
        if (ann.isSingleMemberAnnotationExpr()) {
            SingleMemberAnnotationExpr sm = ann.asSingleMemberAnnotationExpr();
            if (sm.getMemberValue().isStringLiteralExpr()) {
                return sm.getMemberValue().asStringLiteralExpr().getValue();
            }
        } else if (ann.isNormalAnnotationExpr()) {
            NormalAnnotationExpr norm = ann.asNormalAnnotationExpr();
            for (MemberValuePair pair : norm.getPairs()) {
                if ("value".equals(pair.getNameAsString()) || "path".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        return pair.getValue().asStringLiteralExpr().getValue();
                    }
                }
            }
        }
        return "";
    }

    private String extractMethodFromRequestMapping(AnnotationExpr ann) {
        if (ann.isNormalAnnotationExpr()) {
            NormalAnnotationExpr norm = ann.asNormalAnnotationExpr();
            for (MemberValuePair pair : norm.getPairs()) {
                if ("method".equals(pair.getNameAsString())) {
                    return pair.getValue().toString().replaceAll("RequestMethod\\.", "").replaceAll("[\\{\\}]", "").trim();
                }
            }
        }
        return "GET"; // Default
    }

    private String sanitizePath(String path) {
        if (path.isEmpty()) return "/";
        String clean = path.replaceAll("//+", "/");
        if (!clean.startsWith("/")) clean = "/" + clean;
        if (clean.length() > 1 && clean.endsWith("/")) clean = clean.substring(0, clean.length() - 1);
        return clean;
    }

    private void extractJavadocTags(MethodDeclaration method, String relativePath, int line, List<ExtractedElement> elements) {
        method.getJavadoc().ifPresent(javadoc -> {
            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                if (tag.getType() == JavadocBlockTag.Type.PARAM) {
                    tag.getName().ifPresent(name -> {
                        ExtractedElement elem = new ExtractedElement(
                                ElementType.JAVADOC_TAG,
                                relativePath,
                                line,
                                name,
                                tag.getContent().toText()
                        );
                        elem.setParentName(method.getNameAsString());
                        elem.getMetadata().put("tagType", "param");
                        elements.add(elem);
                    });
                } else if (tag.getType() == JavadocBlockTag.Type.RETURN) {
                    ExtractedElement elem = new ExtractedElement(
                            ElementType.JAVADOC_TAG,
                            relativePath,
                            line,
                            "return",
                            tag.getContent().toText()
                    );
                    elem.setParentName(method.getNameAsString());
                    elem.getMetadata().put("tagType", "return");
                    elements.add(elem);
                }
            }
        });
    }

    private void parsePomXml(Path pomPath, List<ExtractedElement> elements) {
        try {
            String content = Files.readString(pomPath);
            // Java version pattern
            Matcher javaMatcher = Pattern.compile("<java\\.version>(.*?)</java\\.version>").matcher(content);
            if (javaMatcher.find()) {
                ExtractedElement javaVer = new ExtractedElement(
                        ElementType.JAVA_VERSION,
                        "pom.xml",
                        1,
                        "java.version",
                        javaMatcher.group(1).trim()
                );
                elements.add(javaVer);
            }

            // Dependency patterns
            Matcher depMatcher = Pattern.compile("<artifactId>(.*?)</artifactId>").matcher(content);
            while (depMatcher.find()) {
                String dep = depMatcher.group(1).trim();
                if (!dep.startsWith("${") && !dep.equals("docdrift")) {
                    ExtractedElement depElem = new ExtractedElement(
                            ElementType.DEPENDENCY,
                            "pom.xml",
                            1,
                            dep,
                            "installed"
                    );
                    elements.add(depElem);
                }
            }
        } catch (Exception e) {
            // Safe fallback
        }
    }

    private void parseProperties(Path propPath, List<ExtractedElement> elements) {
        try {
            List<String> lines = Files.readAllLines(propPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.startsWith("#") && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    ExtractedElement cfg = new ExtractedElement(
                            ElementType.CONFIG_KEY,
                            "application.properties",
                            i + 1,
                            parts[0].trim(),
                            parts.length > 1 ? parts[1].trim() : ""
                    );
                    elements.add(cfg);
                }
            }
        } catch (Exception e) {
            // Safe fallback
        }
    }

    private void parseYml(Path ymlPath, List<ExtractedElement> elements) {
        try {
            List<String> lines = Files.readAllLines(ymlPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.startsWith("#") && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String val = parts.length > 1 ? parts[1].trim() : "";
                    if (!key.isEmpty() && !val.isEmpty()) {
                        ExtractedElement cfg = new ExtractedElement(
                                ElementType.CONFIG_KEY,
                                "application.yml",
                                i + 1,
                                key,
                                val
                        );
                        elements.add(cfg);
                    }
                }
            }
        } catch (Exception e) {
            // Safe fallback
        }
    }
}
