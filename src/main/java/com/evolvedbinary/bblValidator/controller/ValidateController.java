package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ValidationForm;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;

@Controller("/validate")
public class ValidateController {

    // SCENARIO: Form URL Encoded
    // Matches when Content-Type is 'application/x-www-form-urlencoded'
    @Post
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ValidationResponse validateForm(@Body ValidationForm form) {
        return new ValidationResponse(form.schemaId(), form.url(), true, "form handler");
    }

    // SCENARIO: CSV Body + Query param
    // Matches when Content-Type is 'text/csv'
    @Post
    @Consumes(MediaType.TEXT_CSV)
    public ValidationResponse validateCsv(@QueryValue("schema-id") String schemaId, 
                                          @Body String csvContent) {

        return new ValidationResponse(schemaId, csvContent, true, "text handler");
    }

    // SCENARIO: Query Params Only
    // We use ALL here as a fallback, but specific types above take precedence
    @Post
    @Consumes(MediaType.ALL) 
    public ValidationResponse validateParams(@QueryValue("schema-id") String schemaId, 
                                             @QueryValue String url) {
        return new ValidationResponse(schemaId, url, true, "query handler");
    }
}