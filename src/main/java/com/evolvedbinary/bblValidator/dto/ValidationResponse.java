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

    public ValidationResponse(final boolean valid,
                              final List<ValidationError> errors,
                              final long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
        this.valid = valid;
        this.errors = errors != null ? errors : Collections.emptyList();
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

}
