package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Singleton
public class SchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);
    private static final String SCHEMA_DIRECTORY = "schemas";

    private final List<SchemaInfo> schemas = new ArrayList<>();
    private final Map<String, String> schemaContents = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSchemas() {
        try {
            // Load schemas from classpath
            ClassLoader classLoader = getClass().getClassLoader();

            // Get all .json files from the schemas directory
            try (InputStream is = classLoader.getResourceAsStream(SCHEMA_DIRECTORY)) {
                if (is == null) {
                    LOG.warn("Schemas directory not found in classpath");
                    return;
                }
            }

            // Scan for schema metadata files
            loadSchemasFromClasspath();

            LOG.info("Loaded {} schemas from disk", schemas.size());
        } catch (Exception e) {
            LOG.error("Error loading schemas from disk", e);
        }
    }

    private void loadSchemasFromClasspath() {
        try {
            // Get resource URL and list files
            ClassLoader classLoader = getClass().getClassLoader();
            var resource = classLoader.getResource(SCHEMA_DIRECTORY);

            if (resource != null) {
                Path schemaPath = Paths.get(resource.toURI());

                try (Stream<Path> paths = Files.walk(schemaPath, 1)) {
                    paths.filter(path -> path.toString().endsWith(".json"))
                            .forEach(this::loadSchemaMetadata);
                }
            }
        } catch (Exception e) {
            LOG.error("Error scanning schema directory", e);
        }
    }

    private void loadSchemaMetadata(Path metadataPath) {
        try {
            String content = Files.readString(metadataPath, StandardCharsets.UTF_8);
            SchemaInfo schemaInfo = objectMapper.readValue(content, SchemaInfo.class);

            // Load corresponding schema file
            String schemaFileName = metadataPath.getFileName().toString().replace(".json", ".csvs");
            Path schemaFilePath = metadataPath.getParent().resolve(schemaFileName);

            if (Files.exists(schemaFilePath)) {
                String schemaContent = Files.readString(schemaFilePath, StandardCharsets.UTF_8);
                schemaContents.put(schemaInfo.getId(), schemaContent);
                schemas.add(schemaInfo);
                LOG.debug("Loaded schema: {}", schemaInfo.getId());
            } else {
                LOG.warn("Schema file not found for metadata: {}", schemaFileName);
            }
        } catch (IOException e) {
            LOG.error("Error loading schema metadata from: {}", metadataPath, e);
        }
    }

    public List<SchemaInfo> listSchemas() {
        return new ArrayList<>(schemas);
    }

    public String getSchema(String schemaId) throws Exception {
        String content = schemaContents.get(schemaId);

        if (content == null) {
            throw new Exception("Schema not found: " + schemaId);
        }

        return content;
    }
}

