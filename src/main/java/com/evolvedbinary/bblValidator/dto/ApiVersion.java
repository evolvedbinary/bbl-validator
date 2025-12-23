package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

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
