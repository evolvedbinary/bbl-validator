package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ApiVersion;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.evolvedbinary.bblValidator.filter.ApiVersionFilter.BBLVALIDATOR_VERSION_HEADER;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class VersionControllerTest {

    @Inject
    @Client("/version")
    HttpClient client;

    @Value("${api.version}")
    String version;

    @Test
    void getVersion() {
        HttpRequest<Void> request = HttpRequest.GET("/");
        // deserialize the request to ApiVersion
        HttpResponse<ApiVersion> response = client.toBlocking().exchange(request, ApiVersion.class);

        // assert the response status
        assertEquals(HttpStatus.OK, response.getStatus());

        // assert the response type is json
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response returned version
        assertEquals(Optional.of(version), response.getBody().map(ApiVersion::getVersion));
    }
}
