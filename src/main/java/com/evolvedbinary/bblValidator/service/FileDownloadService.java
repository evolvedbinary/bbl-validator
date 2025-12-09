package com.evolvedbinary.bblValidator.service;

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

@Singleton
public class FileDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadService.class);
    private static final String TEMP_DIR_PREFIX = "bbl-validator-";
    private static final String DEFAULT_FILENAME = "downloaded-file.csv";
    private final HttpClient httpClient;

    public FileDownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Downloads a file from the given URL and stores it in a temporary directory.
     *
     * @param url The URL to download from
     * @return Path to the downloaded file in the temp directory
     * @throws IOException if download or file operations fail
     * @throws IllegalArgumentException if url is null or empty
     */
    public Path downloadToTemp(String url) throws IOException {        
        try {
            Path tempDir = createTempDirectory();
            String filename = extractFilename(url);
            Path tempFile = tempDir.resolve(filename);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            try (InputStream inputStream = response.body()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            LOG.info("Downloaded file from {} to {}", url, tempFile);
            return tempFile;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted for URL: " + url, e);
        }
    }

    /**
     * Saves content to a temporary file.
     *
     * @param content The content to save
     * @param filename The filename to use
     * @return Path to the created temp file
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if content or filename is null or empty
     */
    public Path saveContentToTemp(String content, String filename) throws IOException {        
        Path tempDir = createTempDirectory();
        Path tempFile = tempDir.resolve(filename);

        Files.writeString(tempFile, content);

        LOG.info("Saved content to temp file: {}", tempFile);
        return tempFile;
    }

    /**
     * Creates a temporary directory with the standard prefix.
     *
     * @return Path to the created temp directory
     * @throws IOException if directory creation fails
     */
    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory(TEMP_DIR_PREFIX);
    }

    
    /**
     * Extracts filename from URL or returns a default name.
     *
     * @param url The URL to extract filename from
     * @return The extracted filename or default filename
     */
    private String extractFilename(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path == null || path.isEmpty()) {
                return DEFAULT_FILENAME;
            }
            
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                String filename = path.substring(lastSlash + 1);
                // Ensure filename is not empty after extraction
                if (!filename.trim().isEmpty()) {
                    return filename;
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not extract filename from URL: {}", url, e);
        }
        return DEFAULT_FILENAME;
    }
}
