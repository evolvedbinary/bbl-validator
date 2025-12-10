package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// TODO talk to Adam about syncronaztion
@Singleton
public class SchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

    @Value("${schema.directory}")
    private String schemaDirectory;

    private final List<SchemaInfo> schemas = new ArrayList<>();
    private final Map<String, String> schemaContents = new HashMap<>();
    private final Map<String, Path> schemaFilePaths = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSchemas() {
        try {
            // Resolve schema directory path
            Path schemaPath = resolveSchemaPath();

            if (!Files.exists(schemaPath)) {
                LOG.warn("Schemas directory not found: {}", schemaPath);
                return;
            }

            if (!Files.isDirectory(schemaPath)) {
                LOG.warn("Schema path is not a directory: {}", schemaPath);
                return;
            }

            // Scan for schema metadata files
            loadSchemasFromFileSystem(schemaPath);

            LOG.info("Loaded {} schemas from: {}", schemas.size(), schemaPath);
        } catch (Exception e) {
            LOG.error("Error loading schemas from file system", e);
        }
    }

    /**
     * Resolves the schema directory path.
     * If the path starts with a slash, it's treated as an absolute path.
     * Otherwise, it's resolved relative to the current working directory (startup location).
     */
    private Path resolveSchemaPath() {
        if (schemaDirectory.startsWith("/")) {
            // Absolute path
            return Paths.get(schemaDirectory);
        } else {
            // Relative to current working directory
            return Paths.get(System.getProperty("user.dir"), schemaDirectory);
        }
    }

    private void loadSchemasFromFileSystem(Path schemaPath) {
        try (Stream<Path> paths = Files.walk(schemaPath, 1)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            loadSchemaMetadata(path);
                        } catch (Exception e) {
                            LOG.error("Error loading schema metadata from: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            LOG.error("Error scanning schema directory: {}", schemaPath, e);
        }
    }

    private void loadSchemaMetadata(Path metadataPath) throws IOException {
        String content = Files.readString(metadataPath, StandardCharsets.UTF_8);
        SchemaInfo schemaInfo = objectMapper.readValue(content, SchemaInfo.class);

        // Load corresponding schema file
        String schemaFileName = metadataPath.getFileName().toString().replace(".json", ".csvs");
        Path schemaFilePath = metadataPath.getParent().resolve(schemaFileName);

        if (Files.exists(schemaFilePath)) {
            String schemaContent = Files.readString(schemaFilePath, StandardCharsets.UTF_8);
            // TODO: Only load the schema content when needed
            schemaContents.put(schemaInfo.getId(), schemaContent);
            schemaFilePaths.put(schemaInfo.getId(), schemaFilePath);
            schemas.add(schemaInfo);
            LOG.debug("Loaded schema: {}", schemaInfo.getId());
        } else {
            LOG.warn("Schema file not found for metadata: {}", schemaFileName);
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

    public Path getSchemaFilePath(String schemaId) throws Exception {
        Path filePath = schemaFilePaths.get(schemaId);

        if (filePath == null) {
            throw new Exception("Schema file path not found: " + schemaId);
        }

        return filePath;
    }
}

