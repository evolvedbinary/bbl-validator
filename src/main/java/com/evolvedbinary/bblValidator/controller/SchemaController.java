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

    @Inject
    SchemaService schemaService;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public List<SchemaInfo> listSchemas(HttpRequest<?> request) {
        String host = request.getHeaders().get("Host");
        String protocol = request.isSecure() ? "https://" : "http://";

        
        return schemaService.listSchemas().stream()
                .map(schema -> new SchemaInfo(schema.getId(), schema.getName(), schema.getVersion(), schema.getDate(), protocol + host + "/schema/" + schema.getId(), schema.getDescription()))
                .collect(Collectors.toList());
    }

    @Get("/{schema-id}")
    @Produces("text/csv-schema")
    public HttpResponse<Object> getSchema(@PathVariable("schema-id") final String schemaId) {
        final String schema = schemaService.getSchema(schemaId);
        if (schema == null) {
            return HttpResponse
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON_TYPE)
                    .body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        return HttpResponse
                .ok()
                .contentType(MediaType.of("text/csv-schema"))
                .body(schema);
    }
}
