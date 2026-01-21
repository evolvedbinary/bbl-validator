package com.evolvedbinary.bblValidator.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class FileDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadService.class);
    private static final String TEMP_DIR_NAME = "bbl-validator";

    private final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    private final RequestConfig httpRequestConfig;
    private final CloseableHttpClient httpClient;

    private final Path sharedTempDir;
    private final RandomBasedGenerator generator = Generators.randomBasedGenerator();

    public FileDownloadService() {
        this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();

        this.httpRequestConfig = RequestConfig.custom()
        .setConnectTimeout(10_000)
        .setSocketTimeout(10_000)
        .setConnectionRequestTimeout(3_000)
        .build();

        this.httpClient = HttpClients
                .custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(httpRequestConfig)
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();

        try {
            this.sharedTempDir = Files.createTempDirectory(TEMP_DIR_NAME);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to create or access persistent temp directory", e);
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

            final HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new IOException("Non Resolvable url: " + url);
                }

                try (InputStream inputStream = response.getEntity().getContent()) {
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            LOG.trace("Downloaded file from {} to {}", url, tempFile);
            return tempFile;

        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid URL format: " + url, e);
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
