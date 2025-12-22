package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.evolvedbinary.bblValidator.service.SchemaService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.PathVariable;
import jakarta.inject.Inject;

import java.util.List;

@Controller("/schema")
public class SchemaController {

    @Inject
    SchemaService schemaService;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public List<SchemaInfo> listSchemas() {
        return schemaService.listSchemas();
    }

    @Get("/{schema-id}")
    @Produces("text/csv-schema")
    public HttpResponse<Object> getSchema(@PathVariable("schema-id") String schemaId) {
        String schema = schemaService.getSchema(schemaId);
        if (schema == null) {
            return HttpResponse
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON_TYPE)
                    .body(new ErrorResponse(ErrorResponse.Code.SCHEMA_NOT_FOUND,"Schema not found with ID: " + schemaId));
        }
        return HttpResponse.ok(schema);
    }
}
