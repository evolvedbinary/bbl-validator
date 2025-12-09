package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ApiVersion;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller
public class VersionController {

    @Value("${api.version}")
    String version;

    @Get("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersion getVersion() {
        return new ApiVersion(version);
    }
}
