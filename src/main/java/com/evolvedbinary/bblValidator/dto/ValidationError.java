package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object representing a validation error with location information.
 */
@Serdeable
public class ValidationError {
    
    private final String message;
    private final int lineNumber;
    private final int columnIndex;

    public ValidationError(final String message, final int lineNumber, final int columnIndex) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.columnIndex = columnIndex;
    }

    public String getMessage() {
        return message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

}
