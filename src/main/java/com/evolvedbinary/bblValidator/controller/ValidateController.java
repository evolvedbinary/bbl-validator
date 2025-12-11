package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ValidationError;
import com.evolvedbinary.bblValidator.dto.ValidationForm;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
import com.evolvedbinary.bblValidator.service.CsvValidationService;
import com.evolvedbinary.bblValidator.service.FileDownloadService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller("/validate")
public class ValidateController {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateController.class);
    @Inject
    FileDownloadService fileDownloadService;
    @Inject
    CsvValidationService csvValidationService;

    /**
     * Handles form URL encoded validation requests.
     *
     * @param form validation form
     * @return validation response
     */
    @Post
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ValidationResponse validateForm(@Body ValidationForm form) {
        try {
            Path downloadedFile = fileDownloadService.downloadToTemp(form.url());
            LOG.info("File downloaded to: {}", downloadedFile);
            return performValidation(downloadedFile, form.schemaId());
        } catch (IOException e) {
            LOG.error("Failed to download file from URL: {}", form.url(), e);
            return createErrorResponse("Download failed: " + e.getMessage(), 0);
        }
    }

    /**
     * Handles CSV body + query param validation requests.
     *
     * @param schemaId schema ID
     * @param csvContent CSV content
     * @return validation response
     */
    @Post
    @Consumes(MediaType.TEXT_CSV)
    public ValidationResponse validateCsv(@QueryValue("schema-id") String schemaId,
                                          @Body String csvContent) {
        try {
            Path tempFile = fileDownloadService.saveContentToTemp(csvContent);
            LOG.info("CSV content saved to: {}", tempFile);
            return performValidation(tempFile, schemaId);
        } catch (IOException e) {
            LOG.error("Failed to save CSV content to temp file", e);
            return createErrorResponse("Failed to save content: " + e.getMessage(), 0);
        }
    }

    /**
     * Handles query params only validation requests.
     *
     * @param schemaId schema ID
     * @param url URL
     * @return validation response
     */
    @Post
    @Consumes(MediaType.ALL)
    public ValidationResponse validateParams(@QueryValue("schema-id") String schemaId,
                                             @QueryValue String url) {
        try {
            Path downloadedFile = fileDownloadService.downloadToTemp(url);
            LOG.info("File downloaded to: {}", downloadedFile);
            return performValidation(downloadedFile, schemaId);
        } catch (IOException e) {
            LOG.error("Failed to download file from URL: {}", url, e);
            return createErrorResponse("Download failed: " + e.getMessage(), 0);
        }
    }

    private ValidationResponse performValidation(Path csvFile, String schemaId) {
        CsvValidationService.ValidationResult result = csvValidationService.validateCsvFile(csvFile, schemaId);

        if (result.hasErrorMessage()) {
            return createErrorResponse(result.getErrorMessage(), result.getExecutionTimeMs());
        }

        return new ValidationResponse(result.isValid(), result.getErrors(), result.getExecutionTimeMs());
    }

    private ValidationResponse createErrorResponse(String errorMessage, long executionTimeMs) {
        return new ValidationResponse(false,
            List.of(new ValidationError(errorMessage, 0, 0)),
            executionTimeMs);
    }
}