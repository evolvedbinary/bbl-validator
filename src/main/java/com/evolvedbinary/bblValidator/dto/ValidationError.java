package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object representing a validation error with location information.
 */
@Serdeable
public class ValidationError {
    
    private String message;
    private int lineNumber;
    private int columnIndex;

    public ValidationError() {
    }

    public ValidationError(final String message, final int lineNumber, final int columnIndex) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.columnIndex = columnIndex;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(final int columnIndex) {
        this.columnIndex = columnIndex;
    }
}
