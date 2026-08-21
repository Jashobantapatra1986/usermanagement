package com.dppm.usmt.exception;

import com.dppm.usmt.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFoundReturns404Response() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleNotFound(
                new ResourceNotFoundException("User not found with id: 99"),
                exchange("/api/users/99")
        );

        assertError(response, HttpStatus.NOT_FOUND, "User not found with id: 99", "/api/users/99");
    }

    @Test
    void handleBadRequestReturns400ForInvalidWebInput() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBadRequest(
                new ServerWebInputException("Invalid user id"),
                exchange("/api/users/invalid")
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid user id", "/api/users/invalid");
    }

    @Test
    void handleBadRequestReturns400ForIllegalArgument() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleBadRequest(
                new IllegalArgumentException("Email is invalid"),
                exchange("/api/users")
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Email is invalid", "/api/users");
    }

    @Test
    void handleDataIntegrityViolationReturns409Response() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(
                exchange("/api/users")
        );

        assertError(
                response,
                HttpStatus.CONFLICT,
                "The request conflicts with existing data.",
                "/api/users"
        );
    }

    @Test
    void handleResponseStatusUsesStatusAndReasonFromException() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Access is denied"),
                exchange("/api/users/1")
        );

        assertError(response, HttpStatus.FORBIDDEN, "Access is denied", "/api/users/1");
    }

    @Test
    void handleUnexpectedExceptionReturnsGeneric500Response() {
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnexpectedException(
                exchange("/api/users")
        );

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", "/api/users");
    }

    private ServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private void assertError(
            ResponseEntity<ApiErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage,
            String expectedPath
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(expectedStatus.value());
        assertThat(response.getBody().error()).isEqualTo(expectedStatus.getReasonPhrase());
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
        assertThat(response.getBody().path()).isEqualTo(expectedPath);
    }
}
