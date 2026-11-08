package com.hstudio.dto;

import java.time.LocalDateTime;
import java.util.Map;
/** Structure commune des erreurs renvoyees par API. */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    /** Convertit les donn metier en objet de r API. */
    public static ApiErrorResponse from(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
