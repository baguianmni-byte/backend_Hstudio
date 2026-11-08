package com.hstudio.exception;

import com.hstudio.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convertit les erreurs en r HTTP comprehensibles. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            EmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler(InvalidAssignmentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAssignment(
            InvalidAssignmentException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    /** Transforme une ressource absente en r HTTP 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    /** Transforme une op interdite en r HTTP 403. */
    @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of());
    }

    /** Transforme un echec authentification en r HTTP 401. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.UNAUTHORIZED, "Authentification invalide.", request, Map.of());
    }

    /** Presente les erreurs de validation dans une r structuree. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "La requête contient des données invalides.", request, errors);
    }

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return response(HttpStatus.BAD_REQUEST, "La requête contient des données invalides.", request, errors);
    }

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "Le corps de la requête est invalide.", request, Map.of());
    }

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestParameter(
            Exception exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "Un paramètre de la requête est invalide ou manquant.", request, Map.of());
    }

    /** Transforme erreur en r comprehensible pour le client. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return response(status, exception.getReason(), request, Map.of());
    }

    /** Renvoie une erreur generique sans exposer les details internes. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Erreur inattendue sur {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.", request, Map.of());
    }

    /** Construit le format commun des r erreur. */
    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        ApiErrorResponse body = ApiErrorResponse.from(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
