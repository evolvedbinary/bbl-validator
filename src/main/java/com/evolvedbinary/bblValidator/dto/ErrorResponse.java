package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public final class ErrorResponse implements ResponseObject {
    public enum Code {
        SCHEMA_NOT_FOUND, // provided schema id does not exist
        UNEXPECTED_ERROR,
        EMPTY_CSV, // empty body or url content
        NONE_RESOLVABLE_URL, // The url is malformed or do not resolve
        VALIDATION_ERROR // The validation failed
    }

    private final Code code;
    private final String description;

    public ErrorResponse(final Code code, final String description) {
        this.code = code;
        this.description = description;
    }

    public Code getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
