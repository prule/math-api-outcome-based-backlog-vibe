package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CalculationController {

    private final PerformCalculation performCalculation;

    CalculationController(PerformCalculation performCalculation) {
        this.performCalculation = performCalculation;
    }

    @Operation(
            summary = "Perform one Calculation",
            description = "Applies one Operation to Operands a and b and returns the exact decimal Result."
                    + " Only the first failure is reported, in this order: invalid request, unknown operation,"
                    + " operand out of range, division by zero.")
    @ApiResponse(
            responseCode = "200",
            description = "The Calculation and its Result; every decimal is a string.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalculationResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request (malformed JSON, a missing field, a non-numeric Operand or a broken request"
                    + " limit), unknown operation, or an Operand outside BR-1.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                @ExampleObject(name = "invalid-request", value = ProblemExamples.INVALID_REQUEST),
                                @ExampleObject(
                                        name = "operand-out-of-range",
                                        value = ProblemExamples.OPERAND_OUT_OF_RANGE)
                            }))
    @ApiResponse(
            responseCode = "422",
            description = "Valid input that breaks a domain rule: division by zero (BR-2).",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples =
                                    @ExampleObject(
                                            name = "division-by-zero",
                                            value = ProblemExamples.DIVISION_BY_ZERO)))
    @ApiResponse(
            responseCode = "415",
            description = "The body is not application/json.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples =
                                    @ExampleObject(
                                            name = "unsupported-media-type",
                                            value = ProblemExamples.UNSUPPORTED_MEDIA_TYPE)))
    @ApiResponse(
            responseCode = "500",
            description = "An unexpected failure; the detail never exposes the cause.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(name = "unexpected", value = ProblemExamples.UNEXPECTED)))
    @PostMapping(path = "/api/v1/calculations", produces = MediaType.APPLICATION_JSON_VALUE)
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        return CalculationResponse.from(performCalculation.perform(request.operation(), request.a(), request.b()));
    }
}
