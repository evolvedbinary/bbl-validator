package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.ValidationError;
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
        CsvValidatorJavaBridge.ValidationResult result = CsvValidatorJavaBridge.validate(validationRequest);
        final List<FailMessage> errors = result.errors();
        final long executionTime = System.currentTimeMillis() - startTime;
        return processValidationMessages(errors, executionTime);
    }


    private ValidationResult processValidationMessages(final List<FailMessage> messages, final long executionTimeMs) {
        if (messages.isEmpty()) {
            LOG.trace("CSV validation successful - no errors ({}ms)", executionTimeMs);
            return ValidationResult.success(executionTimeMs);
        }

        final List<ValidationError> errors = new ArrayList<>();
        boolean utf8Valid = true;

        for (final FailMessage message : messages) {
            // if one error is a UTF-8 error, then the file is not valid
            if(message.getMessage().startsWith("[UTF-8 Error]")) {
                utf8Valid = false;
            }
            final ValidationError error = new ValidationError(
                message.getMessage(),
                message.getLineNumber(),
                message.getColumnIndex() + 1  // Add 1 for user display
            );
            errors.add(error);
            LOG.trace("Validation error at line {}, column {}: {}", 
                     message.getLineNumber(), message.getColumnIndex(), message.getMessage());
        }

        LOG.trace("CSV validation completed - Valid: false, Errors: {} ({}ms)", errors.size(), executionTimeMs);

        return new ValidationResult(false, errors, executionTimeMs, utf8Valid);
    }


    public static class ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final long executionTimeMs;
        private final boolean utf8Valid;

        public ValidationResult(final boolean valid, final List<ValidationError> errors, final long executionTimeMs, final boolean utf8Valid) {
            this.valid = valid;
            this.errors = errors;
            this.executionTimeMs = executionTimeMs;
            this.utf8Valid = utf8Valid;
        }

        public static ValidationResult success(final long executionTimeMs) {
            return new ValidationResult(true, Collections.emptyList(), executionTimeMs, true);
        }

        public boolean isValid() {
            return valid;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public boolean isUtf8Valid() { return utf8Valid; }
    }
}
