/*
 * Copyright © 2025 Evolved Binary Ltd. (tech@evolvedbinary.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolvedbinary.bbl.validator.controller;

import com.evolvedbinary.bbl.validator.dto.ErrorResponse;
import com.evolvedbinary.bbl.validator.dto.ValidationFailure;
import com.evolvedbinary.bbl.validator.dto.ValidationResponse;
import com.evolvedbinary.bbl.validator.service.CsvValidationService;
import com.evolvedbinary.bbl.validator.service.FileDownloadService;
import com.evolvedbinary.bbl.validator.service.SchemaService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.views.View;
import jakarta.inject.Inject;

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
                model.put("result", new ValidationResponse(result.isPassed(), result.getFailures(), result.getExecutionTime(), result.isUtf8Valid()));
                model.put("errorsTable", getErrorsTable(result.getFailures()));
            } finally {
                Files.delete(tempFile);
            }
        } catch (final IOException e) {
            model.put("error", new ErrorResponse(ErrorResponse.Code.UNEXPECTED_ERROR, "Internal error processing CSV: " + e.getMessage()));
        }

        return model;
    }

    private List<String> getErrorsTable(final List<ValidationFailure> failures) {
        final List<String> table = new ArrayList<>();
        for (final ValidationFailure failure : failures) {
            table.add(failure.getMessage());
        }
        return table;
    }
}
