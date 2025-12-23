package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class SchemaInfo {

    private String id;
    private String name;
    private String version;
    private String date;
    private String url;
    private String description;

    public SchemaInfo() {
    }

    public SchemaInfo(final String id, final String name, final String version, final String date, final String url, final String description) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.date = date;
        this.url = url;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SchemaInfo that)) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

