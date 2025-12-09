package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ValidationResponse {

    private String schemaId;
    private String url;
    private boolean valid;
    private String source;

    public ValidationResponse() {
    }

    public ValidationResponse(String schemaId, String url, boolean valid, String source) {
        this.schemaId = schemaId;
        this.url = url;
        this.valid = valid;
        this.source = source;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
