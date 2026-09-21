# DocDrift (Documentation Decay Detector)

An intelligent framework for detecting, measuring, and tracking inconsistencies between software documentation and Java/Spring Boot implementations.

## Features

- **Automated AST Code Parsing**: Uses JavaParser to extract Spring REST annotations (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, etc.), parameters, return DTO fields, Javadoc tags (`@param`, `@return`), `pom.xml` dependencies, and `application.properties`/`application.yml` configurations.
- **Documentation Parsing**: Parses Markdown (`README.md`, docs) using CommonMark and OpenAPI/Swagger YAML/JSON specifications to extract documented endpoints, parameters, JSON payload keys, and database tables/columns.
- **Database Schema Analysis**: Parses SQL DDL (`.sql`) files using JSqlParser to check schema consistency against documentation.
- **Consistency Engine (DC-01 to DC-10)**: Detects parameter mismatches, removed APIs, undocumented APIs, response field mismatches, HTTP method mismatches, database drift, Javadoc mismatches, README version/dependency drift, configuration mismatches, and spelling errors.
- **Documentation Health Score (DHS)**: Calculates a mathematically defensible health score ($0-100$) and assigns a decay level (**HEALTHY**, **MILD DECAY**, **MODERATE DECAY**, **SEVERE DECAY**).
- **Interactive Web Dashboard**: Embedded web frontend accessible at `http://localhost:8080/` to visualize DHS breakdown, browse side-by-side findings, filter by category/severity, and export reports in JSON or Markdown format.
- **Sample Demo Project**: Pre-packaged demo project included under `samples/demo-project/` for out-of-the-box analysis.

## Requirements

- **Java**: JDK 17 or higher
- **Maven**: 3.8+ (or use IDE built-in Maven)
- **IDE**: IntelliJ IDEA (Community or Ultimate)

## How to Run in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select **File -> Open...** and select the `DocDrift` project directory (`e:\Projects\DocDrift`).
3. IntelliJ will automatically detect it as a Maven project and sync dependencies.
4. Run `DocDriftApplication.java` located at:
   `src/main/java/org/docdrift/DocDriftApplication.java`
5. Open your browser and navigate to:
   [http://localhost:8080/](http://localhost:8080/)

## How to Run via Command Line

```bash
# Build the project and run tests
mvn clean package

# Run the Spring Boot application
java -jar target/docdrift-1.0.0-SNAPSHOT.jar
```

## REST API Endpoints

- `GET /api/projects`: List registered projects
- `POST /api/projects/register`: Register a local project directory or path for analysis
- `POST /api/projects/{projectId}/analyze`: Trigger consistency analysis run
- `GET /api/projects/{projectId}/runs/latest`: Get latest health score report & findings
- `GET /api/projects/{projectId}/export/markdown`: Export analysis findings as Markdown report
- `POST /api/findings/{findingId}/status`: Mark finding as `OPEN`, `FALSE_POSITIVE`, or `ACCEPTED`
