package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Collections;
import java.util.List;

/**
 * Response object representing the result of a validation operation.
 */
@Serdeable
public final class ValidationResponse implements ResponseObject {

    private final boolean valid;
    private final List<ValidationError> errors;
    private final long executionTimeMs;
    private final boolean utf8Valid;

    public ValidationResponse(final boolean valid,
                              final List<ValidationError> errors,
                              final long executionTimeMs,
                              final boolean utf8Valid) {
        this.executionTimeMs = executionTimeMs;
        this.valid = valid;
        this.errors = errors != null ? errors : Collections.emptyList();
        this.utf8Valid = utf8Valid;
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
