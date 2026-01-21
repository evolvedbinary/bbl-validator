package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.ResponseObject;
import com.evolvedbinary.bblValidator.dto.ValidationForm;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
import com.evolvedbinary.bblValidator.service.CsvValidationService;
import com.evolvedbinary.bblValidator.service.FileDownloadService;
import com.evolvedbinary.bblValidator.service.SchemaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
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
import java.nio.file.Files;
import java.nio.file.Path;

@Controller("/validate")
public class ValidateController {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateController.class);
    @Inject
    FileDownloadService fileDownloadService;
    @Inject
    CsvValidationService csvValidationService;
    @Inject
    SchemaService schemaService;

    /**
     * Handles form URL encoded validation requests.
     *
     * @param form validation form
     * @return validation response
     */
    @Post
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<ResponseObject> validateForm(@Body final ValidationForm form) {
        if (null == schemaService.getSchema(form.schemaId())) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + form.schemaId()));
        }
        try {
            final Path downloadedFile = fileDownloadService.downloadToTemp(form.url());
            LOG.trace("File downloaded to: {}", downloadedFile);
            try {
                return HttpResponse.ok(performValidation(downloadedFile, form.schemaId()));
            } finally {
                Files.delete(downloadedFile);
            }
        } catch (final IOException e) {
            LOG.trace("Failed to download file from URL: {}", form.url());
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NON_RESOLVABLE_URL,"Unable to resolve url : " + form.url()));
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
    public HttpResponse<ResponseObject> validateCsv(@QueryValue("schema-id") final String schemaId,
                                                    @Nullable @Body final String csvContent) {
        if (schemaService.getSchema(schemaId) == null) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        if (csvContent == null || csvContent.isEmpty()) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NO_CSV,"Empty CSV content"));
        }
            try {
                final Path tempFile = fileDownloadService.saveContentToTemp(csvContent);
                try {
                    LOG.trace("CSV content saved to: {}", tempFile);
                    return HttpResponse.ok(performValidation(tempFile, schemaId));
                } finally {
                    Files.delete(tempFile);
                }
            } catch (final IOException e) {
                LOG.error("Failed to save CSV content to temp file", e);
                return HttpResponse.serverError().body(new ErrorResponse(ErrorResponse.Code.UNEXPECTED_ERROR,"Unable to store CSV: " + e.getMessage()));
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
    public HttpResponse<ResponseObject> validateParams(@QueryValue("schema-id") final String schemaId,
                                                       @QueryValue final String url) {
        if (schemaService.getSchema(schemaId) == null) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        try {
            final Path downloadedFile = fileDownloadService.downloadToTemp(url);
            LOG.trace("File downloaded to: {}", downloadedFile);
            try {
                return HttpResponse.ok(performValidation(downloadedFile, schemaId));
            } finally {
                Files.delete(downloadedFile);
            }
        } catch (final IOException e) {
            LOG.trace("Failed to download file from URL: {}", url);
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NON_RESOLVABLE_URL,"Unable to resolve url : " + url));
        }
    }

    private ResponseObject performValidation(final Path csvFile, final String schemaId) {
        final CsvValidationService.ValidationResult result = csvValidationService.validateCsvFile(csvFile, schemaId);

        return new ValidationResponse(result.isPassed(), result.getFailures(), result.getExecutionTime(), result.isUtf8Valid());
    }
}