package com.paulrule.mathapi.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Operands arrive as JSON numbers or numeric strings; both bind to {@link BigDecimal} without loss. */
record CalculationRequest(
        // allowed values are generated from the domain in config.OpenApiConfig
        @Schema(description = "The Operation to perform, exactly as named (case-sensitive).", example = "DIVIDE")
        @NotNull
        String operation,

        @Schema(
                types = {"number", "string"},
                description = OPERAND,
                example = "1")
        @NotNull
        BigDecimal a,

        @Schema(
                types = {"number", "string"},
                description = OPERAND,
                example = "3")
        @NotNull
        BigDecimal b) {

    private static final String OPERAND = "A decimal Operand, as a JSON number or a numeric string (send a string to"
            + " keep every digit through clients that parse JSON numbers as floating point). At most 100 significant"
            + " digits and a scale within ±1000 (BR-1).";
}
