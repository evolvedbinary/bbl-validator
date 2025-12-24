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
        Map<String, Object> model = new HashMap<>(); 
        model.put("version", version);
        model.put("schemas", schemaService.listSchemas());
        return model;
    }

    @View("validate")
    @Post(value = "/validate", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> validateSubmit(@Body Map<String, String> formData) {
        String schemaId = formData.get("schemaId");
        String csvContent = formData.get("csvContent");

        Map<String, Object> model = new HashMap<>();
        model.put("version", version);
        model.put("schemas", schemaService.listSchemas());
        model.put("schemaId", schemaId);
        model.put("csvContent", csvContent);

        // if (schemaId == null || schemaId.trim().isEmpty()) {
        //     model.put("error", new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND, "Schema ID is required"));
        //     return model;
        // }
        
        // if (schemaService.getSchema(schemaId) == null) {
        //     model.put("error", new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND, "Schema not found with ID: " + schemaId));
        //     return model;
        // }

        // if (csvContent == null || csvContent.isEmpty()) {
        //     model.put("error", new ErrorResponse(ErrorResponse.Code.NO_CSV, "Empty CSV content"));
        //     return model;
        // }

        try {
            Path tempFile = fileDownloadService.saveContentToTemp(csvContent);
            CsvValidationService.ValidationResult result = csvValidationService.validateCsvFile(tempFile, schemaId);


            if (result.hasErrorMessage()) {
                model.put("error", new ErrorResponse(ErrorResponse.Code.VALIDATION_ERROR, result.getErrorMessage()));
            } else {
                model.put("result", new ValidationResponse(result.isValid(), result.getErrors(), result.getExecutionTimeMs()));
                model.put("errorsTable", getErrorsTable(result.getErrors()));
            }

        } catch (IOException e) {
            model.put("error", new ErrorResponse(ErrorResponse.Code.UNEXPECTED_ERROR, "Internal error processing CSV: " + e.getMessage()));
        }

        return model;
    }

    private List<String> getErrorsTable(List<ValidationError> errors) {
        List<String> table = new ArrayList<>();
        for (ValidationError error : errors) {
            table.add(error.getMessage());
        }
        return table;
    }
}
