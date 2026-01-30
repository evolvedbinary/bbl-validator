/*
 * Copyright © 2025 Evolved Binary Ltd. (tech@evolvedbinary.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
