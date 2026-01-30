/*
 * Copyright © 2025 Evolved Binary Ltd. (tech@evolvedbinary.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ApiVersion;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.evolvedbinary.bblValidator.filter.ApiVersionFilter.BBLVALIDATOR_VERSION_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class VersionControllerTest {

    @Inject
    @Client("/version")
    HttpClient client;

    @Value("${api.version}")
    String version;

    @Test
    void getVersion() {
        final HttpRequest<Void> request = HttpRequest.GET("/");
        // deserialize the request to ApiVersion
        final HttpResponse<ApiVersion> response = client.toBlocking().exchange(request, ApiVersion.class);

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
