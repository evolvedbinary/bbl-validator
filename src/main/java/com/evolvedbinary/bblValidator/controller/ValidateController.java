package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.*;
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
        if(null == schemaService.getSchema(form.schemaId())) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + form.schemaId()));
        }
        try {
            final Path downloadedFile = fileDownloadService.downloadToTemp(form.url());
            LOG.debug("File downloaded to: {}", downloadedFile);
            return HttpResponse.ok(performValidation(downloadedFile, form.schemaId()));
        } catch (final IOException e) {
            LOG.debug("Failed to download file from URL: {}", form.url());
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NONE_RESOLVABLE_URL,"Unable to resolve url : " + form.url()));
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
        if(schemaService.getSchema(schemaId) == null) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        if(csvContent == null || csvContent.isEmpty()) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.EMPTY_CSV,"Empty CSV content"));
        }
        try {
            final Path tempFile = fileDownloadService.saveContentToTemp(csvContent);
            LOG.debug("CSV content saved to: {}", tempFile);
            return HttpResponse.ok(performValidation(tempFile, schemaId));
        } catch (final IOException e) {
            // TODO ASK Adam if this should be an error and wake someone from sleep
            LOG.error("Failed to save CSV content to temp file", e);
            // what's the issue here excalty??
            // we didn't manage to save the given file to disk
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NONE_RESOLVABLE_URL,"Unable to : " + schemaId));
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
        if(schemaService.getSchema(schemaId) == null) {
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        try {
            final Path downloadedFile = fileDownloadService.downloadToTemp(url);
            LOG.debug("File downloaded to: {}", downloadedFile);
            return HttpResponse.ok(performValidation(downloadedFile, schemaId));
        } catch (final IOException e) {
            LOG.debug("Failed to download file from URL: {}", url);
            return HttpResponse.badRequest().body(new ErrorResponse(ErrorResponse.Code.NONE_RESOLVABLE_URL,"Unable to resolve url : " + url));
        }
    }

    private ResponseObject performValidation(final Path csvFile, final String schemaId) {
        final CsvValidationService.ValidationResult result = csvValidationService.validateCsvFile(csvFile, schemaId);

        if (result.hasErrorMessage()) {
            return new ErrorResponse(ErrorResponse.Code.VALIDATION_ERROR,"An error occurred: " + result.getErrorMessage());
        }

        return new ValidationResponse(result.isValid(), result.getErrors(), result.getExecutionTimeMs());
    }
}