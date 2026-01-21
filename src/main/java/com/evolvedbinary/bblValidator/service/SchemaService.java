package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import net.jcip.annotations.GuardedBy;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

@Singleton
public class SchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

    @Value("${schema.directory}")
    private String schemaDirectory;

    private final List<SchemaInfo> schemas = new ArrayList<>();
    private final ReadWriteLock schemaLock = new ReentrantReadWriteLock();
    @GuardedBy("schemaLock")
    private final Map<String, String> schemaContents = new HashMap<>();
    @GuardedBy("schemaLock")
    private final Map<String, Path> schemaFilePaths = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSchemas() {
        try {
            // Resolve schema directory path
            final Path schemaPath = resolveSchemaPath();

            if (!Files.exists(schemaPath)) {
                LOG.error("Schemas directory not found: {}", schemaPath);
                throw new IllegalStateException("Schemas directory not found: " + schemaPath);
            }

            if (!Files.isDirectory(schemaPath)) {
                LOG.error("Schema path is not a directory: {}", schemaPath);
                throw new IllegalStateException("Schema path is not a directory: " + schemaPath);
            }

            // Scan for schema metadata files
            loadSchemasFromFileSystem(schemaPath);

            LOG.trace("Loaded {} schemas from: {}", schemas.size(), schemaPath);
        } catch (final SecurityException e) {
            LOG.error("You don't have enough permissions to open this file: ", e);
            throw new IllegalStateException("You don't have permission to open Schema directory ");

        }
    }

    /**
     * Resolves the schema directory path.
     * If the path starts with a slash, it's treated as an absolute path.
     * Otherwise, it's resolved relative to the current working directory (startup location).
     */
    private Path resolveSchemaPath() {
        final Path schemaPath =  Paths.get(schemaDirectory);
        if (schemaPath.isAbsolute()) {
            // Absolute path
            return schemaPath;
        } else {
            // Relative to current working
            final Path applicationDir = Paths.get(System.getProperty("user.dir"));
            return applicationDir.resolve(schemaPath);
        }
    }

    private void loadSchemasFromFileSystem(final Path schemaPath) {
        try (Stream<Path> paths = Files.walk(schemaPath, 1)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            loadSchemaMetadata(path);
                        } catch (final IOException e) {
                            LOG.warn("Error loading schema metadata from: {}", path, e);
                        }
                    });
        } catch (final IOException e) {
            LOG.warn("Error scanning schema directory: {}", schemaPath, e);
        }
    }

    private void loadSchemaMetadata(final Path metadataPath) throws IOException {
        final String content = Files.readString(metadataPath, StandardCharsets.UTF_8);
        final SchemaInfo schemaInfo = objectMapper.readValue(content, SchemaInfo.class);

        // Load corresponding schema file
        final String schemaFileName = metadataPath.getFileName().toString().replace(".json", ".csvs");
        final Path schemaFilePath = metadataPath.getParent().resolve(schemaFileName);

        if (Files.exists(schemaFilePath)) {
            final String schemaContent = Files.readString(schemaFilePath, StandardCharsets.UTF_8);
            schemaLock.writeLock().lock();
            try {
                schemaContents.put(schemaInfo.getId(), schemaContent);
                schemaFilePaths.put(schemaInfo.getId(), schemaFilePath);
            } finally {
                schemaLock.writeLock().unlock();
            }
            schemas.add(schemaInfo);
            LOG.trace("Loaded schema: {}", schemaInfo.getId());
        } else {
            LOG.warn("Schema file not found for metadata: {}", schemaFileName);
        }
    }

    public List<SchemaInfo> listSchemas() {
        return schemas;
    }

    public String getSchema(final String schemaId) {
        schemaLock.readLock().lock();
        try {
            return schemaContents.get(schemaId);
        } finally {
            schemaLock.readLock().unlock();
        }
    }

    public Path getSchemaFilePath(final String schemaId) {
        schemaLock.readLock().lock();
        try {
            return schemaFilePaths.get(schemaId);
        } finally {
            schemaLock.readLock().unlock();
        }
    }
}

