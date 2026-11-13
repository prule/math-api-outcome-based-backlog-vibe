package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.UnknownOperationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The single place errors become RFC 9457 Problem Details. Extending {@link ResponseEntityExceptionHandler} makes
 * framework errors (405, 415, ...) Problem Details too.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final URI INVALID_REQUEST = URI.create("/problems/invalid-request");
    static final URI UNKNOWN_OPERATION = URI.create("/problems/unknown-operation");

    @ExceptionHandler
    ProblemDetail unknownOperation(UnknownOperationException e) {
        return problem(HttpStatus.BAD_REQUEST, UNKNOWN_OPERATION, "Unknown operation", e.getMessage());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // the parser's own message names internal types, so it is never echoed
        return invalidRequest(
                "The body must be a JSON object with 'operation', 'a' and 'b'; operands must be numbers or numeric strings.");
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String missing = ex.getFieldErrors().stream()
                .map(error -> "'" + error.getField() + "'")
                .sorted()
                .collect(Collectors.joining(", "));
        return invalidRequest("Missing required field(s): " + missing + ".");
    }

    private static ResponseEntity<Object> invalidRequest(String detail) {
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Invalid request", detail));
    }

    private static ProblemDetail problem(HttpStatus status, URI type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        return problem;
    }
}
