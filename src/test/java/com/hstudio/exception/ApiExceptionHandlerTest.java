package com.hstudio.exception;

import com.hstudio.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** V la r associe a une ressource absente. */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler exceptionHandler = new ApiExceptionHandler();

    /** V qu une ressource absente produit une r 404 structuree. */
    @Test
    void returnsStructuredNotFoundResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demandes/unknown");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleNotFound(
                new ResourceNotFoundException("Demande introuvable."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Demande introuvable.");
        assertThat(response.getBody().path()).isEqualTo("/api/demandes/unknown");
        assertThat(response.getBody().validationErrors()).isEmpty();
    }

}
