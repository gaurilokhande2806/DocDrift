package org.docdrift.analyzer;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.docdrift.model.dto.ExtractedElement;
import org.docdrift.model.enums.ElementType;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class DbAnalyzer {

    public List<ExtractedElement> analyzeProjectDatabaseSchema(String projectPath) {
        List<ExtractedElement> elements = new ArrayList<>();
        Path root = Paths.get(projectPath);

        if (!Files.exists(root)) {
            return elements;
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.toString().endsWith(".sql"))
                    .forEach(path -> parseSqlFile(path, elements));
        } catch (Exception e) {
            // Ignore unreadable files
        }

        return elements;
    }

    private void parseSqlFile(Path sqlFile, List<ExtractedElement> elements) {
        String fileName = sqlFile.getFileName().toString();
        try {
            String content = Files.readString(sqlFile);
            String[] statements = content.split(";");

            for (String sqlStmt : statements) {
                String trimmed = sqlStmt.trim();
                if (trimmed.toLowerCase().startsWith("create table")) {
                    try {
                        Statement stmt = CCJSqlParserUtil.parse(trimmed);
                        if (stmt instanceof CreateTable createTable) {
                            String tableName = createTable.getTable().getName().replaceAll("`", "");

                            ExtractedElement tableElem = new ExtractedElement(
                                    ElementType.DB_TABLE,
                                    fileName,
                                    1,
                                    tableName,
                                    "TABLE"
                            );
                            elements.add(tableElem);

                            if (createTable.getColumnDefinitions() != null) {
                                for (ColumnDefinition col : createTable.getColumnDefinitions()) {
                                    String colName = col.getColumnName().replaceAll("`", "");
                                    String colType = col.getColDataType().getDataType();

                                    ExtractedElement colElem = new ExtractedElement(
                                            ElementType.DB_COLUMN,
                                            fileName,
                                            1,
                                            colName,
                                            colType
                                    );
                                    colElem.setParentName(tableName);
                                    elements.add(colElem);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Regex fallback if JSqlParser fails on custom vendor syntax
                        parseSqlRegexFallback(trimmed, fileName, elements);
                    }
                }
            }
        } catch (Exception e) {
            // Safe fallback
        }
    }

    private void parseSqlRegexFallback(String sql, String fileName, List<ExtractedElement> elements) {
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([a-zA-Z0-9_]+)`?", Pattern.CASE_INSENSITIVE);
        Matcher tMatcher = tablePattern.matcher(sql);
        if (tMatcher.find()) {
            String tableName = tMatcher.group(1);
            ExtractedElement tableElem = new ExtractedElement(
                    ElementType.DB_TABLE,
                    fileName,
                    1,
                    tableName,
                    "TABLE"
            );
            elements.add(tableElem);

            Pattern colPattern = Pattern.compile("`?([a-zA-Z0-9_]+)`?\\s+(VARCHAR|INT|BIGINT|TEXT|DATETIME|BOOLEAN|TIMESTAMP)", Pattern.CASE_INSENSITIVE);
            Matcher cMatcher = colPattern.matcher(sql);
            while (cMatcher.find()) {
                String colName = cMatcher.group(1);
                String colType = cMatcher.group(2);

                ExtractedElement colElem = new ExtractedElement(
                        ElementType.DB_COLUMN,
                        fileName,
                        1,
                        colName,
                        colType
                );
                colElem.setParentName(tableName);
                elements.add(colElem);
            }
        }
    }
}
