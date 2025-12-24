package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Response object representing an error that occurred during validation.
 */
@Serdeable
public final class ErrorResponse implements ResponseObject {
    public enum Code {
        /**
         * provided schema id does not exist
         */
        SCHEMA_NOT_FOUND,

        /**
         * A fatal error has occurred
         */
        //TODO(YB)
        UNEXPECTED_ERROR,
        NO_CSV, // empty body or url content
        NON_RESOLVABLE_URL, // The url is malformed or do not resolve
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
