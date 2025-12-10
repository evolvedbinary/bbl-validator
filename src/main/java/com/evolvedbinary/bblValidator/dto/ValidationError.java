package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ValidationError {
    
    private String message;
    private int lineNumber;
    private int columnIndex;

    public ValidationError() {
    }

    public ValidationError(String message, int lineNumber, int columnIndex) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.columnIndex = columnIndex;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }
}
