package com.evolvedbinary.bblValidator.controller;

import com.evolvedbinary.bblValidator.dto.ErrorResponse;
import com.evolvedbinary.bblValidator.dto.ValidationResponse;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class ValidateControllerTest {
    @Inject
    @Client("/validate")
    HttpClient client;

    @Value("${api.version}")
    String version;

    private static final String BBLVALIDATOR_VERSION_HEADER = "X-BBLVALIDATOR-VERSION";

    @Value("${schema.directory}")
    String schemaTestDirectory;

    @Test
    void uploadAndValidateCsv() throws IOException {
        Path validCsvFile = Path.of(schemaTestDirectory, "concatPass.csv");
        String csvContent = Files.readString(validCsvFile);
        
        HttpRequest<String> request = HttpRequest.POST("/?schema-id=concat", csvContent)
                .contentType(MediaType.TEXT_CSV);
        
        HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        assertEquals(version, response.getHeaders().get(BBLVALIDATOR_VERSION_HEADER));
        
        assertTrue(response.getBody().isPresent());

        ValidationResponse validationResponse = response.getBody().get();
        
        assertTrue(validationResponse.isValid());
        assertTrue(validationResponse.getErrors().isEmpty());
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
    }

    @Test
    void uploadAndValidateInvalidCsv() throws IOException {
        Path invalidCsvFile = Path.of(schemaTestDirectory, "concatFail.csv");
        String csvContent = Files.readString(invalidCsvFile);
        
        HttpRequest<String> request = HttpRequest.POST("/?schema-id=concat", csvContent)
                .contentType(MediaType.TEXT_CSV);
        
        HttpResponse<ValidationResponse> response = client.toBlocking().exchange(request, ValidationResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), response.getContentType());
        
        assertTrue(response.getBody().isPresent());

        ValidationResponse validationResponse = response.getBody().get();
        
        assertFalse(validationResponse.isValid());
        assertFalse(validationResponse.getErrors().isEmpty());

        validationResponse.getErrors().forEach(error -> {
            assertNotNull(error.getMessage());
            assertFalse(error.getMessage().isEmpty());
            assertTrue(error.getLineNumber() > 0);
            assertTrue(error.getColumnIndex() >= 0);
        });
        
        String errorMessage = "is(concat($c1, $c2)) fails for row: 3, column: c3, value: \"ccccc\"";

        assertEquals(errorMessage, validationResponse.getErrors().getFirst().getMessage());
        assertEquals(3, validationResponse.getErrors().getFirst().getLineNumber());
        assertEquals(3, validationResponse.getErrors().getFirst().getColumnIndex());
        
        assertTrue(validationResponse.getExecutionTimeMs() > -1);
    }

    @Test
    void uploadAndValidateCsvWithNonExistingSchema() throws IOException {
        Path validCsvFile = Path.of(schemaTestDirectory, "concatPass.csv");
        String csvContent = Files.readString(validCsvFile);

        HttpRequest<String> request = HttpRequest.POST("/?schema-id=nonExistingSchema", csvContent)
                .contentType(MediaType.TEXT_CSV);
        // Http client consider any response outside the 2xx range as exception
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.SCHEMA_NOT_FOUND, errorBody.getCode());
        assertEquals("Schema not found with ID: nonExistingSchema", errorBody.getDescription());
    }

    @Test
    void uploadAndValidateCsvWithoutContent() throws IOException {
        String csvContent = "";

        HttpRequest<String> request = HttpRequest.POST("/?schema-id=concat", csvContent)
                .contentType(MediaType.TEXT_CSV);
        // Http client consider any response outside the 2xx range as exception
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NO_CSV, errorBody.getCode());
        assertEquals("Empty CSV content", errorBody.getDescription());
    }

    @Test
    void provideUrlAndValidateCsvFromForm() {
        fail("TODO implement");
    }

    @Test
    void provideUrlAndValidateInvalidCsvFromForm() {
        fail("TODO implement");
    }

    @Test
    void provideUrlAndValidateCsvInQueryString() {
        fail("TODO implement");
    }

    @Test
    void provideUrlAndValidateInvalidCsvInQueryString() {
        fail("TODO implement");
    }

    @Test
    void provideNonResolvableUrlAndValidateCsvFromForm() {
        fail("TODO implement");
    }

    @Test
    void provideNonCsvUrlAndValidateCsvFromForm() {
        fail("TODO implement");
    }

    @Test
    void provideNonResolvableUrlAndValidateCsvInQueryString() {

        HttpRequest<Void> request = HttpRequest.POST("/?schema-id=concat&url=nothing", null);

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, String.class));

        // assert the response status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // assert the X-BBLVALIDATOR-VERSION is present and it's returning the expected value.
        assertEquals(version, exception.getResponse().getHeaders().get(BBLVALIDATOR_VERSION_HEADER));

        // assert the response type
        assertEquals(Optional.of(MediaType.APPLICATION_JSON_TYPE), exception.getResponse().getContentType());

        // Get the error response
        ErrorResponse errorBody = exception.getResponse().getBody(ErrorResponse.class).orElse(null);
        assertNotNull(errorBody);
        assertEquals(ErrorResponse.Code.NON_RESOLVABLE_URL, errorBody.getCode());
        assertEquals("Unable to resolve url : nothing", errorBody.getDescription());
    }

    @Test
    void provideNonCsvUrlAndValidateCsvInQueryString() {
        fail("TODO implement");
    }

}
