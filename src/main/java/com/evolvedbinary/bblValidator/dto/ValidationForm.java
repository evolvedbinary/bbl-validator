package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ValidationForm(
        String schemaId,
        String url) {
}
