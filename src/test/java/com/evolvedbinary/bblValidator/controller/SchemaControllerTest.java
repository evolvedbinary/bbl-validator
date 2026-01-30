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


import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.SchemaInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.evolvedbinary.bblValidator.controller.SchemaController.CSV_SCHEMA_MEDIA_TYPE;
import static com.evolvedbinary.bblValidator.filter.ApiVersionFilter.BBLVALIDATOR_VERSION_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class SchemaControllerTest {
    @Inject
    @Client("/schema")
    HttpClient client;

    @Value("${api.version}")
    String version;


    @Test
    void schemaList() throws JsonProcessingException {
        final HttpRequest<Void> request = HttpRequest.GET("/");
        // get the raw response
        final HttpResponse<String> response = client.toBlocking().exchange(request, String.class);

        // assert the response status
        assertEquals(HttpStatus.OK, response.getStatus());

        // assert the response type is json
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the body is not null
        assertTrue(response.getBody().isPresent());
        final String body = response.getBody().get();
        // Parse the raw json
        final ObjectMapper mapper = new ObjectMapper();
        final List<SchemaInfo> list = mapper.readValue(body, new TypeReference<>() {});

        // expected schema Info
        final SchemaInfo expectedSchemaInfo = new SchemaInfo();
        expectedSchemaInfo.setId("concat");
        expectedSchemaInfo.setName("concat");
        expectedSchemaInfo.setDate("2015-11-01");
        expectedSchemaInfo.setVersion("1.0.0");
        expectedSchemaInfo.setUrl("https://localhost/concat.csvs");
        expectedSchemaInfo.setDescription("sample file for testing");

        // expected schema list
        final List<SchemaInfo> expectedList = List.of(
                expectedSchemaInfo
        );

        // asset the returned list is equal to the expect one
        assertEquals(expectedList, list);
    }

    @Test
    void getSchema() {
        final String expectedSchema = "version 1.1\r\n" +
                "@totalColumns 3\r\n" +
                "c1:\r\n" +
                "c2:\r\n" +
                "c3: is(concat($c1,$c2))\r\n";
        final HttpRequest<Void> request = HttpRequest.GET("/concat");
        // deserialize the request to SchemaInfo
        final HttpResponse<String> response = client.toBlocking().exchange(request, String.class);

        // assert the response status
        assertEquals(HttpStatus.OK, response.getStatus());

        // assert the response type is "text/csv-schema"
        assertEquals(Optional.of(CSV_SCHEMA_MEDIA_TYPE), response.getContentType());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the returned schema
        assertEquals(Optional.of(expectedSchema), response.getBody());
    }

    @Test
    void getInvalidSchema() {
        final HttpRequest<Void> request = HttpRequest.GET("/none");
        // Http client consider any response outside the 2xx range as exception
        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.SCHEMA_NOT_FOUND, errorBody.getCode());
        assertEquals("Schema not found with ID: none", errorBody.getDescription());
    }
}
