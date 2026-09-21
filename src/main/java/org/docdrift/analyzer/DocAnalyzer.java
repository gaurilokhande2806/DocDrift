package org.docdrift.analyzer;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.Schema;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.enums.ElementType;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class DocAnalyzer {

    private final Parser markdownParser = Parser.builder().build();

    public List<ExtractedElement> analyzeProjectDocumentation(String projectPath) {
        List<ExtractedElement> elements = new ArrayList<>();
        Path root = Paths.get(projectPath);

        if (!Files.exists(root)) {
            return elements;
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".md")) {
                    parseMarkdownFile(path, elements);
                } else if (fileName.contains("openapi") || fileName.contains("swagger") ||
                        fileName.endsWith("openapi.yaml") || fileName.endsWith("openapi.json")) {
                    parseOpenApiFile(path, elements);
                }
            });
        } catch (Exception e) {
            // Ignore unreadable files
        }

        return elements;
    }

    private void parseMarkdownFile(Path mdFile, List<ExtractedElement> elements) {
        try {
            String content = Files.readString(mdFile);
            String fileName = mdFile.getFileName().toString();

            // 1. Extract Documented HTTP Endpoints (e.g. POST /api/users, GET /api/products)
            Pattern endpointPattern = Pattern.compile("(GET|POST|PUT|DELETE|PATCH)\\s+(/[a-zA-Z0-9_/\\{\\}\\-]+)");
            Matcher epMatcher = endpointPattern.matcher(content);
            while (epMatcher.find()) {
                String method = epMatcher.group(1).toUpperCase();
                String path = epMatcher.group(2);

                ExtractedElement endpointElem = new ExtractedElement(
                        ElementType.ENDPOINT,
                        fileName,
                        getLineNumber(content, epMatcher.start()),
                        path,
                        method
                );
                elements.add(endpointElem);
            }

            // 2. Extract Documented Parameters (e.g. "accepts name and email", "parameters: name, email")
            Pattern paramPattern = Pattern.compile("(?:accepts|parameters|params|requires|fields)\\s*[:\\-]?\\s*([a-zA-Z0-9_\\s,]+)", Pattern.CASE_INSENSITIVE);
            Matcher paramMatcher = paramPattern.matcher(content);
            while (paramMatcher.find()) {
                String rawParams = paramMatcher.group(1);
                String[] tokens = rawParams.split("[,\\s+]+");
                for (String token : tokens) {
                    token = token.trim();
                    if (token.length() > 1 && !token.equalsIgnoreCase("and") && !token.equalsIgnoreCase("or")) {
                        ExtractedElement pElem = new ExtractedElement(
                                ElementType.PARAMETER,
                                fileName,
                                getLineNumber(content, paramMatcher.start()),
                                token,
                                "documented"
                        );
                        elements.add(pElem);
                    }
                }
            }

            // 3. Extract Documented Java / Version dependencies (e.g. "Java 11", "Java 17")
            Pattern javaVerPattern = Pattern.compile("Java\\s+(8|11|17|21)", Pattern.CASE_INSENSITIVE);
            Matcher jvMatcher = javaVerPattern.matcher(content);
            if (jvMatcher.find()) {
                ExtractedElement jvElem = new ExtractedElement(
                        ElementType.JAVA_VERSION,
                        fileName,
                        getLineNumber(content, jvMatcher.start()),
                        "java.version",
                        jvMatcher.group(1)
                );
                elements.add(jvElem);
            }

            // 4. Extract Documented Configuration Keys (e.g. port 8080, server.port=8080)
            Pattern configPattern = Pattern.compile("([a-zA-Z0-9\\._\\-]+)\\s*[:=]\\s*([a-zA-Z0-9\\._\\-]+)");
            Matcher cfgMatcher = configPattern.matcher(content);
            while (cfgMatcher.find()) {
                String key = cfgMatcher.group(1).trim();
                String val = cfgMatcher.group(2).trim();
                if (key.contains(".") || key.equalsIgnoreCase("port")) {
                    ExtractedElement cfgElem = new ExtractedElement(
                            ElementType.CONFIG_KEY,
                            fileName,
                            getLineNumber(content, cfgMatcher.start()),
                            key,
                            val
                    );
                    elements.add(cfgElem);
                }
            }

            // 5. Extract Database Table/Column references in Markdown
            Pattern dbPattern = Pattern.compile("(?:table|column)\\s+`?([a-zA-Z0-9_]+)`?", Pattern.CASE_INSENSITIVE);
            Matcher dbMatcher = dbPattern.matcher(content);
            while (dbMatcher.find()) {
                String dbName = dbMatcher.group(1);
                ExtractedElement dbElem = new ExtractedElement(
                        ElementType.DB_COLUMN,
                        fileName,
                        getLineNumber(content, dbMatcher.start()),
                        dbName,
                        "documented"
                );
                elements.add(dbElem);
            }

        } catch (Exception e) {
            // Safe fallback
        }
    }

    private void parseOpenApiFile(Path apiFile, List<ExtractedElement> elements) {
        try {
            String fileName = apiFile.getFileName().toString();
            OpenAPI openAPI = new OpenAPIV3Parser().read(apiFile.toString());
            if (openAPI == null || openAPI.getPaths() == null) return;

            openAPI.getPaths().forEach((path, pathItem) -> {
                Map<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> ops = pathItem.readOperationsMap();
                ops.forEach((httpMethod, operation) -> {
                    ExtractedElement endpointElem = new ExtractedElement(
                            ElementType.ENDPOINT,
                            fileName,
                            1,
                            path,
                            httpMethod.name().toUpperCase()
                    );
                    elements.add(endpointElem);

                    // OpenAPI Parameters
                    if (operation.getParameters() != null) {
                        for (Parameter parameter : operation.getParameters()) {
                            ExtractedElement paramElem = new ExtractedElement(
                                    ElementType.PARAMETER,
                                    fileName,
                                    1,
                                    parameter.getName(),
                                    parameter.getIn() != null ? parameter.getIn() : "query"
                            );
                            paramElem.setParentName(path);
                            paramElem.getMetadata().put("httpMethod", httpMethod.name().toUpperCase());
                            elements.add(paramElem);
                        }
                    }

                    // OpenAPI Request Body schema properties
                    if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
                        operation.getRequestBody().getContent().forEach((mediaType, mediaObj) -> {
                            if (mediaObj.getSchema() != null && mediaObj.getSchema().getProperties() != null) {
                                Map<String, Schema> props = mediaObj.getSchema().getProperties();
                                props.forEach((propName, propSchema) -> {
                                    ExtractedElement paramElem = new ExtractedElement(
                                            ElementType.PARAMETER,
                                            fileName,
                                            1,
                                            propName,
                                            propSchema.getType() != null ? propSchema.getType() : "string"
                                    );
                                    paramElem.setParentName(path);
                                    paramElem.getMetadata().put("httpMethod", httpMethod.name().toUpperCase());
                                    elements.add(paramElem);
                                });
                            }
                        });
                    }
                });
            });
        } catch (Exception e) {
            // Safe fallback
        }
    }

    private int getLineNumber(String content, int characterIndex) {
        int line = 1;
        for (int i = 0; i < characterIndex && i < content.length(); i++) {
            if (content.charAt(i) == '\n') line++;
        }
        return line;
    }
}
