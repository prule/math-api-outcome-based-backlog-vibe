package com.paulrule.mathapi.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Operands arrive as JSON numbers or numeric strings; both bind to {@link BigDecimal} without loss. Documented values
 * that come from the domain (allowed operations, BR-1 limits) are added in config.OpenApiConfig.
 */
record CalculationRequest(
        @Schema(description = "The Operation to perform, exactly as named (case-sensitive).", example = "DIVIDE")
        @NotNull
        String operation,

        @Schema(
                types = {"number", "string"},
                example = "1")
        @NotNull
        BigDecimal a,

        @Schema(
                types = {"number", "string"},
                example = "3")
        @NotNull
        BigDecimal b) {}
