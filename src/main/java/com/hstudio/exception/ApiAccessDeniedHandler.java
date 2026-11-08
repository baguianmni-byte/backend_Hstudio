package com.hstudio.exception;

import com.hstudio.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/** Formate la r JSON lorsque les droits sont insuffisants. */
@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** Ecrit une r JSON indiquant que les droits sont insuffisants. */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.from(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Vous n'êtes pas autorisé à effectuer cette action.",
                request.getRequestURI(),
                Map.of()
        ));
    }
}
