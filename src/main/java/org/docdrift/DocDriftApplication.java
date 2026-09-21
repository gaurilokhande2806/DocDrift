package org.docdrift;

import org.docdrift.model.entity.ProjectEntity;
import org.docdrift.service.AnalysisService;
import org.docdrift.service.IngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class DocDriftApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocDriftApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDemoData(IngestionService ingestionService, AnalysisService analysisService) {
        return args -> {
            try {
                Path samplePath = Paths.get("samples/demo-project").toAbsolutePath();
                if (samplePath.toFile().exists()) {
                    ProjectEntity demoProject = ingestionService.registerProject(
                            "Demo Spring Boot Store",
                            samplePath.toString(),
                            "Sample Spring Boot project with intentional documentation decay for demonstration."
                    );
                    analysisService.runAnalysis(demoProject.getId(), "v1.0.0");
                }
            } catch (Exception e) {
                // Ignore initialization errors for demo project if path not created yet
            }
        };
    }
}
