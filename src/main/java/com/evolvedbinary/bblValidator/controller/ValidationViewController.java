package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.ValidationError;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
import com.evolvedbinary.bblValidator.service.CsvValidationService;
import com.evolvedbinary.bblValidator.service.FileDownloadService;
import com.evolvedbinary.bblValidator.service.SchemaService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/views")
public class ValidationViewController {

    @Inject
    CsvValidationService csvValidationService;
    @Inject
    FileDownloadService fileDownloadService;
    @Inject
    SchemaService schemaService;

    @Value("${api.version}")
    String version;

    @View("validate")
    @Get("/validate")
    public Map<String, Object> validate() {
        final Map<String, Object> model = new HashMap<>(); 
        model.put("version", version);
        model.put("schemas", schemaService.listSchemas());
        model.put("csvSource", "url");
        return model;
    }

    @View("validate")
    @Post(value = "/validate", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> validateSubmit(@Body final Map<String, String> formData) {
        final String schemaId = formData.get("schemaId");
        final String csvSource = formData.get("csvSource");
        final String csvUrl = formData.get("csvUrl");
        final String csvContent = formData.get("csvContent");

        final Map<String, Object> model = new HashMap<>();
        model.put("version", version);
        model.put("schemas", schemaService.listSchemas());
        model.put("schemaId", schemaId);
        model.put("csvSource", csvSource);
        model.put("csvUrl", csvUrl);
        model.put("csvContent", csvContent);


        if ((csvContent == null || csvContent.isEmpty()) && (csvUrl == null || csvUrl.isEmpty())) {
            model.put("error", new ErrorResponse(ErrorResponse.Code.NO_CSV, "Please provide either CSV content or CSV URL"));
            return model;
        }

        final boolean isUrl = csvSource.equals("url");

        try {
            final Path tempFile = isUrl ? fileDownloadService.downloadToTemp(csvUrl) : fileDownloadService.saveContentToTemp(csvContent);
            try {
                final CsvValidationService.ValidationResult result = csvValidationService.validateCsvFile(tempFile, schemaId);
                model.put("result", new ValidationResponse(result.isValid(), result.getErrors(), result.getExecutionTimeMs(), result.isUtf8Valid()));
                model.put("errorsTable", getErrorsTable(result.getErrors()));
            } finally {
                Files.delete(tempFile);
            }
        } catch (final IOException e) {
            model.put("error", new ErrorResponse(ErrorResponse.Code.UNEXPECTED_ERROR, "Internal error processing CSV: " + e.getMessage()));
        }

        return model;
    }

    private List<String> getErrorsTable(final List<ValidationError> errors) {
        final List<String> table = new ArrayList<>();
        for (final ValidationError error : errors) {
            table.add(error.getMessage());
        }
        return table;
    }
}
