package com.evolvedbinary.bblValidator.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import jakarta.inject.Singleton;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Singleton
public class FileDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadService.class);
    private static final String TEMP_DIR_NAME = "bbl-validator";

    private final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    private final RequestConfig httpRequestConfig;

    private final Path sharedTempDir;
    private final RandomBasedGenerator generator = Generators.randomBasedGenerator();

    public FileDownloadService() {
        this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        this.poolingHttpClientConnectionManager.setMaxTotal(20);
        this.poolingHttpClientConnectionManager.setDefaultMaxPerRoute(15);
        this.poolingHttpClientConnectionManager.setValidateAfterInactivity(TimeValue.ofMilliseconds(15_000));

        this.httpRequestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(10_000))
                .setResponseTimeout(Timeout.ofMilliseconds(10_000))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(3_000))
                .build();

        try {
            this.sharedTempDir = Files.createTempDirectory(TEMP_DIR_NAME);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to create or access persistent temp directory", e);
        }
    }

    private CloseableHttpClient buildHttpClient() {
        return HttpClients
                .custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(httpRequestConfig)
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .evictExpiredConnections()
                .build();
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

            try (final CloseableHttpResponse response = buildHttpClient().execute(httpGet)) {

                final int statusCode = response.getCode();

                if (statusCode != HttpStatus.SC_OK) {
                    throw new IOException("Non Resolvable url: " + url);
                }

                try (final InputStream inputStream = response.getEntity().getContent()) {
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