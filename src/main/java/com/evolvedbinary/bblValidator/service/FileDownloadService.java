package com.evolvedbinary.bblValidator.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Singleton
public class FileDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadService.class);
    private static final String TEMP_DIR_NAME = "bbl-validator";
    private final HttpClient httpClient;
    private final Path sharedTempDir;
    private final RandomBasedGenerator generator = Generators.randomBasedGenerator();

    public FileDownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        try {
            //TODO: save the name for clean up
            this.sharedTempDir = Files.createTempDirectory(TEMP_DIR_NAME);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create or access persistent temp directory", e);
        }
    }

    /**
     * Downloads a file from the given URL and stores it with a UUID v4 filename.
     *
     * @param url The URL to download from
     * @return Path to the downloaded file in the shared temp directory
     * @throws IOException if download or file operations fail
     */
    public Path downloadToTemp(final String url) throws IOException {
        try {
            final String filename = generateUuidFilename();
            final Path tempFile = sharedTempDir.resolve(filename);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            final HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            try (InputStream inputStream = response.body()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            LOG.trace("Downloaded file from {} to {}", url, tempFile);
            return tempFile;

        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid URL format: " + url, e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted for URL: " + url, e);
        }
    }

    /**
     * Saves content to a temporary file with a UUID v4 filename.
     *
     * @param content The content to save
     * @return Path to the created temp file
     * @throws IOException if file operations fail
     */
    public Path saveContentToTemp(final String content) throws IOException {
        final String uuidFilename = generateUuidFilename();
        final Path tempFile = sharedTempDir.resolve(uuidFilename);

        Files.writeString(tempFile, content);

        LOG.trace("Saved content to temp file: {}", tempFile);
        return tempFile;
    }

    /**
     * Generates a UUID v4 filename.
     *
     * @return A UUID v4 string to be used as filename
     */
    private String generateUuidFilename() {
        final UUID uuid = generator.generate();
        return uuid.toString() + ".csv";
    }
}
