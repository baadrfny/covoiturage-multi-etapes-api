
package com.covoiturage.covoiturage_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        return buildProblem(
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : "Erreur de requête",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Données invalides");

        return buildProblem(400, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return buildProblem(
                400,
                "Paramètre obligatoire manquant : " + ex.getParameterName(),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return buildProblem(409, "Conflit de données : contrainte d'unicité violée", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        return buildProblem(
                500,
                "Une erreur interne est survenue",
                request
        );
    }

    private ProblemDetail buildProblem(
            int status,
            String message,
            HttpServletRequest request) {

        HttpStatus httpStatus = HttpStatus.valueOf(status);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                httpStatus,
                message
        );

        problem.setTitle(httpStatus.getReasonPhrase());
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", LocalDateTime.now().toString());
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("message", message);

        return problem;
    }
}
