package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.DivisionByZeroException;
import com.paulrule.mathapi.domain.UnknownOperationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    static final URI INVALID_REQUEST = URI.create("/problems/invalid-request");
    static final URI UNKNOWN_OPERATION = URI.create("/problems/unknown-operation");
    static final URI DIVISION_BY_ZERO = URI.create("/problems/division-by-zero");

    @ExceptionHandler
    ProblemDetail unknownOperation(UnknownOperationException e) {
        return problem(HttpStatus.BAD_REQUEST, UNKNOWN_OPERATION, "Unknown operation", e.getMessage());
    }

    @ExceptionHandler
    ProblemDetail divisionByZero(DivisionByZeroException e) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, DIVISION_BY_ZERO, "Division by zero", e.getMessage());
    }

    /** Anything unforeseen still answers in the API's error format, without leaking the cause. */
    @ExceptionHandler
    ProblemDetail unexpected(Exception e) {
        log.error("Unexpected error", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.");
        problem.setTitle("Internal Server Error");
        return problem;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // the parser's own message names internal types, so it is never echoed
        return invalidRequest("The body is not valid JSON, or an operand is not a number or numeric string.");
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String missing = ex.getFieldErrors().stream()
                .map(error -> "'" + error.getField() + "'")
                .sorted()
                .collect(Collectors.joining(", "));
        return invalidRequest("Missing or empty field(s): " + missing + ".");
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
