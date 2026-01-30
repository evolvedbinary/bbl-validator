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
package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.ValidationFailure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.csv.validator.api.java.CsvValidatorJavaBridge;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;
import uk.gov.nationalarchives.csv.validator.api.java.Substitution;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class CsvValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvValidationService.class);

    @Inject
    private SchemaService schemaService;

    public ValidationResult validateCsvFile(final Path csvFilePath, final String schemaId) {
        final String schemaFilePath = String.valueOf(schemaService.getSchemaFilePath(schemaId));
        final Charset csvEncoding = StandardCharsets.UTF_8;
        final boolean validateUtf8Encoding = true;
        final Charset csvSchemaEncoding = StandardCharsets.UTF_8;
        final boolean failFast = false;
        final List<Substitution> pathSubstitutions = Collections.emptyList();
        final boolean enforceCaseSensitivePathChecks = false;
        final boolean trace = false;
        final boolean skipFileChecks = false;

        final CsvValidatorJavaBridge.ValidationRequest validationRequest = new CsvValidatorJavaBridge.ValidationRequest(
                csvFilePath.toString(), csvEncoding, validateUtf8Encoding, schemaFilePath,
                csvSchemaEncoding, true, failFast, pathSubstitutions,
                enforceCaseSensitivePathChecks, trace, null, skipFileChecks, -1);

        final long startTime = System.currentTimeMillis();
        final CsvValidatorJavaBridge.ValidationResult result = CsvValidatorJavaBridge.validate(validationRequest);
        final List<FailMessage> errors = result.errors();
        final long executionTime = System.currentTimeMillis() - startTime;
        return processValidationMessages(errors, executionTime);
    }


    private ValidationResult processValidationMessages(final List<FailMessage> messages, final long executionTime) {
        if (messages.isEmpty()) {
            LOG.trace("CSV validation successful - no errors ({}ms)", executionTime);
            return ValidationResult.success(executionTime);
        }

        final List<ValidationFailure> errors = new ArrayList<>();
        boolean utf8Valid = true;

        for (final FailMessage message : messages) {
            // if one error is a UTF-8 error, then the file is not valid
            if(message.getMessage().startsWith("[UTF-8 Error]")) {
                utf8Valid = false;
            }
            final ValidationFailure error = new ValidationFailure(
                message.getMessage(),
                message.getLineNumber(),
                message.getColumnIndex() + 1  // Add 1 for user display
            );
            errors.add(error);
            LOG.trace("Validation error at line {}, column {}: {}", 
                     message.getLineNumber(), message.getColumnIndex(), message.getMessage());
        }

        LOG.trace("CSV validation completed - Valid: false, Errors: {} ({}ms)", errors.size(), executionTime);

        return new ValidationResult(false, errors, executionTime, utf8Valid);
    }


    public static class ValidationResult {
        private final boolean passed;
        private final List<ValidationFailure> failures;
        private final long executionTime;
        private final boolean utf8Valid;

        public ValidationResult(final boolean passed, final List<ValidationFailure> failures, final long executionTime, final boolean utf8Valid) {
            this.passed = passed;
            this.failures = failures;
            this.executionTime = executionTime;
            this.utf8Valid = utf8Valid;
        }

        public static ValidationResult success(final long executionTime) {
            return new ValidationResult(true, Collections.emptyList(), executionTime, true);
        }

        public boolean isPassed() {
            return passed;
        }

        public List<ValidationFailure> getFailures() {
            return failures;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public boolean isUtf8Valid() { return utf8Valid; }
    }
}
