package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object representing a validation failure with location information.
 */
@Serdeable
public class ValidationFailure {
    
    private final String message;
    private final int line;
    private final int column;

    public ValidationFailure(final String message, final int line, final int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
