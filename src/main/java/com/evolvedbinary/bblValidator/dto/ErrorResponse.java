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
        UNEXPECTED_ERROR,

        /**
         * No csv content to validate
         */
        NO_CSV,

        /**
         * The url is malformed or do not resolve
         */
        NON_RESOLVABLE_URL,
        
        /**
         * The validation failed
         */
        VALIDATION_ERROR
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
