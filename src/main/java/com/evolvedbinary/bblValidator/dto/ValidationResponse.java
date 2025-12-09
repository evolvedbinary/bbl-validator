package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

@Serdeable
public class ValidationResponse {

    private boolean valid;
    private List<ValidationError> errors;
    private long executionTimeMs;

    public ValidationResponse() {
        this.errors = new ArrayList<>();
    }

    public ValidationResponse(boolean valid,
                            List<ValidationError> errors, long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
    }


    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
