package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object representing the API version information.
 */
@Serdeable
public class ApiVersion {

    private String version;

    public ApiVersion() {
    }

    public ApiVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
