package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ValidationForm;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
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

/**
 * Controller for handling validation requests.
 */
@Controller("/validate")
public class ValidateController {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateController.class);
    @Inject
    FileDownloadService fileDownloadService;

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
            // TODO: Perform validation with downloadedFile
            return new ValidationResponse(form.schemaId(), form.url(), true, "form handler");
        } catch (IOException e) {
            LOG.error("Failed to download file from URL: {}", form.url(), e);
            return new ValidationResponse(form.schemaId(), form.url(), false, "Download failed: " + e.getMessage());
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
            Path tempFile = fileDownloadService.saveContentToTemp(csvContent, "uploaded-content.csv");
            LOG.info("CSV content saved to: {}", tempFile);
            // TODO: Perform validation with tempFile
            return new ValidationResponse(schemaId, "CSV content", true, "text handler");
        } catch (IOException e) {
            LOG.error("Failed to save CSV content to temp file", e);
            return new ValidationResponse(schemaId, "CSV content", false, "Failed to save content: " + e.getMessage());
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
            // TODO: Perform validation with downloadedFile
            return new ValidationResponse(schemaId, url, true, "query handler");
        } catch (IOException e) {
            LOG.error("Failed to download file from URL: {}", url, e);
            return new ValidationResponse(schemaId, url, false, "Download failed: " + e.getMessage());
        }
    }
}