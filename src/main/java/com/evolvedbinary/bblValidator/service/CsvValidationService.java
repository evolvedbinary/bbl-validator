package com.evolvedbinary.bblValidator.service;

import com.evolvedbinary.bblValidator.dto.ValidationError;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.csv.validator.api.java.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.nationalarchives.csv.validator.api.CsvValidator$.MODULE$;


@Singleton
public class CsvValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvValidationService.class);
    private static final int DEFAULT_MAX_CHARS_PER_CELL = 8096;

    @Inject
    private SchemaService schemaService;

    public ValidationResult validateCsvFile(Path csvFilePath, String schemaId) {
        long startTime = System.currentTimeMillis();
        try {
            String schemaFilePath = String.valueOf(schemaService.getSchemaFilePath(schemaId));                        
            Charset csvEncoding = MODULE$.DEFAULT_ENCODING();
            boolean validateUtf8Encoding = csvEncoding.name().equals("UTF-8");
            Charset csvSchemaEncoding = MODULE$.DEFAULT_ENCODING();
            boolean failFast = false;
            List<Substitution> pathSubstitutions = new ArrayList<>();
            boolean enforceCaseSensitivePathChecks = false;
            boolean trace = false;
            boolean skipFileChecks = false;

            CsvValidatorJavaBridge.ValidationRequest validationRequest = new CsvValidatorJavaBridge.ValidationRequest(
                    csvFilePath.toString(), csvEncoding, validateUtf8Encoding, schemaFilePath, 
                    csvSchemaEncoding, true, failFast, pathSubstitutions, 
                    enforceCaseSensitivePathChecks, trace, null, skipFileChecks, DEFAULT_MAX_CHARS_PER_CELL);
            
            CsvValidatorJavaBridge.ValidationResult result = CsvValidatorJavaBridge.validate(validationRequest);
            List<FailMessage> errors = result.errors();
            long executionTime = System.currentTimeMillis() - startTime;
            return processValidationMessages(errors, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            LOG.error("Error validating CSV file: {}", csvFilePath, e);
            return ValidationResult.error("Validation failed: " + e.getMessage(), executionTime);
        }
    }


    private ValidationResult processValidationMessages(List<FailMessage> messages, long executionTimeMs) {
        if (messages.isEmpty()) {
            LOG.info("CSV validation successful - no errors ({}ms)", executionTimeMs);
            return ValidationResult.success(executionTimeMs);
        }

        List<ValidationError> errors = new ArrayList<>();

        for (FailMessage message : messages) {
            ValidationError error = new ValidationError(
                message.getMessage(),
                message.getLineNumber(),
                message.getColumnIndex() + 1  // Add 1 for user display
            );
            errors.add(error);
            LOG.info("Validation error at line {}, column {}: {}", 
                     message.getLineNumber(), message.getColumnIndex(), message.getMessage());
        }

        LOG.info("CSV validation completed - Valid: false, Errors: {} ({}ms)", errors.size(), executionTimeMs);
        return new ValidationResult(false, errors, executionTimeMs);
    }


    public static class ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final String errorMessage;
        private final long executionTimeMs;

        public ValidationResult(boolean valid, List<ValidationError> errors, long executionTimeMs) {
            this.valid = valid;
            this.errors = errors;
            this.errorMessage = null;
            this.executionTimeMs = executionTimeMs;
        }

        private ValidationResult(String errorMessage, long executionTimeMs) {
            this.valid = false;
            this.errors = new ArrayList<>();
            this.errorMessage = errorMessage;
            this.executionTimeMs = executionTimeMs;
        }

        public static ValidationResult success(long executionTimeMs) {
            return new ValidationResult(true, new ArrayList<>(), executionTimeMs);
        }

        public static ValidationResult error(String errorMessage, long executionTimeMs) {
            return new ValidationResult(errorMessage, executionTimeMs);
        }

        public boolean isValid() {
            return valid;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasErrorMessage() {
            return errorMessage != null;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
    }
}
