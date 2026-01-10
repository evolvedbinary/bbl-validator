package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.evolvedbinary.bblValidator.service.SchemaService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.PathVariable;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Controller("/schema")
public class SchemaController {
    public static final MediaType CSV_SCHEMA_MEDIA_TYPE = MediaType.of("text/csv-schema");

    @Inject
    SchemaService schemaService;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public List<SchemaInfo> listSchemas(final HttpRequest<?> request) {
        final String host = request.getHeaders().get("Host");
        final String path = request.getPath().replace("/schema", "/schema/");
        final String protocol = request.isSecure() ? "https://" : "http://";
        final String url = protocol + host + path;

        return schemaService.listSchemas().stream()
                .map(schema -> new SchemaInfo(schema.getId(), schema.getName(), schema.getVersion(), schema.getDate(), url + schema.getId(), schema.getDescription()))
                .collect(Collectors.toList());
    }

    @Get("/{schema-id}")
    @Produces("text/csv-schema")
    public HttpResponse<Object> getSchema(@PathVariable("schema-id") final String schemaId) {
        final String schema = schemaService.getSchema(schemaId);
        if (schema == null) {
            return HttpResponse
                    .notFound()
                    .contentType(MediaType.APPLICATION_JSON_TYPE)
                    .body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        return HttpResponse
                .ok()
                .contentType(CSV_SCHEMA_MEDIA_TYPE)
                .body(schema);
    }
}
