package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.evolvedbinary.bblValidator.service.SchemaService;
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
    public String getSchema(@PathVariable("schema-id") String schemaId) throws Exception {
        return schemaService.getSchema(schemaId);
    }
}
