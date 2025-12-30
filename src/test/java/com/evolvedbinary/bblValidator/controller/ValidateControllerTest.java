package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.evolvedbinary.bblValidator.filter.ApiVersionFilter.BBLVALIDATOR_VERSION_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
public class ValidateControllerTest {
    @Inject
    @Client("/validate")
    HttpClient client;

    @Value("${api.version}")
    String version;
    
    @Value("${schema.directory}")
    String schemaTestDirectory;

    @Test
    void uploadAndValidateCsv() throws IOException {
        final Path validCsvFile = Path.of(schemaTestDirectory, "concatPass.csv");
        final String csvContent = Files.readString(validCsvFile);
        
        final MutableHttpRequest<String> request = HttpRequest.POST("/", csvContent)
                .contentType(MediaType.TEXT_CSV);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", "concat");
        
        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));
        
        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();
        
        assertTrue(validationResponse.isValid());
        assertTrue(validationResponse.getErrors().isEmpty());
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
    }

    @Test
    void uploadAndValidateInvalidCsv() throws IOException {
        final Path invalidCsvFile = Path.of(schemaTestDirectory, "concatFail.csv");
        final String csvContent = Files.readString(invalidCsvFile);

        final MutableHttpRequest<String> request = HttpRequest.POST("/", csvContent)
                .contentType(MediaType.TEXT_CSV);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", "concat");
        
        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        
        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();
        
        assertFalse(validationResponse.isValid());
        assertFalse(validationResponse.getErrors().isEmpty());

        validationResponse.getErrors().forEach(error -> {
            assertNotNull(error.getMessage());
            assertFalse(error.getMessage().isEmpty());
            assertTrue(error.getLineNumber() > 0);
            assertTrue(error.getColumnIndex() >= 0);
        });
        
        final String errorMessage = "is(concat($c1, $c2)) fails for row: 3, column: c3, value: \"ccccc\"";

        assertEquals(errorMessage, validationResponse.getErrors().getFirst().getMessage());
        assertEquals(3, validationResponse.getErrors().getFirst().getLineNumber());
        assertEquals(3, validationResponse.getErrors().getFirst().getColumnIndex());
        
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
    }

    @Test
    void uploadAndValidateCsvWithNonExistingSchema() throws IOException {
        final Path validCsvFile = Path.of(schemaTestDirectory, "concatPass.csv");
        final String csvContent = Files.readString(validCsvFile);

        final MutableHttpRequest<String> request = HttpRequest.POST("/", csvContent)
                .contentType(MediaType.TEXT_CSV);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", "nonExistingSchema");

        // Http client consider any response outside the 2xx range as exception
        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.SCHEMA_NOT_FOUND, errorBody.getCode());
        assertEquals("Schema not found with ID: nonExistingSchema", errorBody.getDescription());
    }

    @Test
    void uploadAndValidateCsvWithoutContent() throws IOException {
        final String csvContent = "";

        final MutableHttpRequest<String> request = HttpRequest.POST("/", csvContent)
                .contentType(MediaType.TEXT_CSV);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", "concat");

        // Http client consider any response outside the 2xx range as exception
        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NO_CSV, errorBody.getCode());
        assertEquals("Empty CSV content", errorBody.getDescription());
    }

    @Test
    void provideUrlAndValidateCsvFromForm() {
        final String url = "https://raw.githubusercontent.com/marmoure/bbl-validator/refs/heads/feature/vlidation/src/test/resources/schemas/concatPass.csv";
        final String schemaId = "concat";
        final Map<String, String> formBody = Map.of(
                "schemaId", schemaId,
                "url", url
        );

        final HttpRequest<Map<String, String>> request = HttpRequest.POST("/", formBody)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();

        assertTrue(validationResponse.isValid());
        assertTrue(validationResponse.getErrors().isEmpty());
        assertTrue(validationResponse.getExecutionTimeMs() > -1);

    }

    @Test
    void provideUrlAndValidateInvalidCsvFromForm() {
        final String url = "https://raw.githubusercontent.com/marmoure/bbl-validator/refs/heads/feature/vlidation/src/test/resources/schemas/concatFail.csv";
        final String schemaId = "concat";
        final Map<String, String> formBody = Map.of(
                "schemaId", schemaId,
                "url", url
        );

        final HttpRequest<Map<String, String>> request = HttpRequest.POST("/", formBody)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();
        
        assertFalse(validationResponse.isValid());
        assertFalse(validationResponse.getErrors().isEmpty());

        validationResponse.getErrors().forEach(error -> {
            assertNotNull(error.getMessage());
            assertFalse(error.getMessage().isEmpty());
            assertTrue(error.getLineNumber() > 0);
            assertTrue(error.getColumnIndex() >= 0);
        });
        
        final String errorMessage = "is(concat($c1, $c2)) fails for row: 3, column: c3, value: \"ccccc\"";

        assertEquals(errorMessage, validationResponse.getErrors().getFirst().getMessage());
        assertEquals(3, validationResponse.getErrors().getFirst().getLineNumber());
        assertEquals(3, validationResponse.getErrors().getFirst().getColumnIndex());
        
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
        
    }

    @Test
    void provideUrlAndValidateCsvInQueryString() {
        final String url = "https://raw.githubusercontent.com/marmoure/bbl-validator/refs/heads/feature/vlidation/src/test/resources/schemas/concatPass.csv";
        final String schemaId = "concat";

        final MutableHttpRequest<Void> request = HttpRequest.POST("/", null);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", schemaId);
        params.add("url", url);

        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();

        assertTrue(validationResponse.isValid());
        assertTrue(validationResponse.getErrors().isEmpty());
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
    }

    @Test
    void provideUrlAndValidateInvalidCsvInQueryString() {
        final String url = "https://raw.githubusercontent.com/marmoure/bbl-validator/refs/heads/feature/vlidation/src/test/resources/schemas/concatFail.csv";
        final String schemaId = "concat";

        final MutableHttpRequest<Void> request = HttpRequest.POST("/", null);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", schemaId);
        params.add("url", url);

        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        
        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();
        
        assertFalse(validationResponse.isValid());
        assertFalse(validationResponse.getErrors().isEmpty());

        validationResponse.getErrors().forEach(error -> {
            assertNotNull(error.getMessage());
            assertFalse(error.getMessage().isEmpty());
            assertTrue(error.getLineNumber() > 0);
            assertTrue(error.getColumnIndex() >= 0);
        });
        
        final String errorMessage = "is(concat($c1, $c2)) fails for row: 3, column: c3, value: \"ccccc\"";

        assertEquals(errorMessage, validationResponse.getErrors().getFirst().getMessage());
        assertEquals(3, validationResponse.getErrors().getFirst().getLineNumber());
        assertEquals(3, validationResponse.getErrors().getFirst().getColumnIndex());
        
        assertTrue(validationResponse.getExecutionTimeMs() > -1);

    }

    @Test
    void provideNonResolvableUrlAndValidateCsvFromForm() {
        //TODO(YB) seek help
        final String url = "https://static.evolvedbinary.com/404.csv";
        final String schemaId = "concat";
        final Map<String, String> formBody = Map.of(
                "schemaId", schemaId,
                "url", url
        );

        final HttpRequest<Map<String, String>> request = HttpRequest.POST("/", formBody)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NON_RESOLVABLE_URL, errorBody.getCode());
        assertEquals("Unable to resolve url : nothing", errorBody.getDescription());
    }

    @Test
    void provideInvalidUrlFormatAndValidateCsvFromForm() {
        final String url = "nothing";
        final String schemaId = "concat";
        final Map<String, String> formBody = Map.of(
                "schemaId", schemaId,
                "url", url
        );

        final HttpRequest<Map<String, String>> request = HttpRequest.POST("/", formBody)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NON_RESOLVABLE_URL, errorBody.getCode());
        assertEquals("Unable to resolve url : nothing", errorBody.getDescription());
    }

    @Test
    void provideNonCsvUrlAndValidateCsvFromForm() {
        final String url = "https://picsum.photos/200/300";
        final String schemaId = "concat";
        final Map<String, String> formBody = Map.of(
                "schemaId", schemaId,
                "url", url
        );

        final HttpRequest<Map<String, String>> request = HttpRequest.POST("/", formBody)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        assertTrue(response.getBody().isPresent());

        final ValidationResponse validationResponse = response.getBody().get();
        
        assertFalse(validationResponse.isValid());
        assertFalse(validationResponse.getErrors().isEmpty());

        assertTrue(validationResponse.getExecutionTimeMs() > -1);

    }

    @Test
    void provideInvalidUrlFormatAndValidateCsvInQueryString() {
        final String schemaId = "concat";
        final String url = "nothing";

        final MutableHttpRequest<Void> request = HttpRequest.POST("/", null);
        final MutableHttpParameters params = request.getParameters();
        params.add("schema-id", schemaId);
        params.add("url", url);

        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NON_RESOLVABLE_URL, errorBody.getCode());
        assertEquals("Unable to resolve url : nothing", errorBody.getDescription());
    }

    @Test
    void provideNonResolvableUrlAndValidateCsvInQueryString() {
        //TODO(YB)
        final HttpRequest<Void> request = HttpRequest.POST("/?schema-id=concat&url=nothing", null);

        final HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        final ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NON_RESOLVABLE_URL, errorBody.getCode());
        assertEquals("Unable to resolve url : nothing", errorBody.getDescription());
    }

    @Test
    void provideNonCsvUrlAndValidateCsvInQueryString() {
        fail("TODO implement");
    }

}
