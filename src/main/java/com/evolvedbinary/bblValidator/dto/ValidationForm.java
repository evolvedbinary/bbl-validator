package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object representing a validation request form.
 */
@Serdeable
public record ValidationForm(
        String schemaId,
        String url) {
}
