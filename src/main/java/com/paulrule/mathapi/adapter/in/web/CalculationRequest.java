package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Operands arrive as JSON numbers or numeric strings; both bind to {@link BigDecimal} without loss. */
record CalculationRequest(
        @NotNull String operation,
        @NotNull BigDecimal a,
        @NotNull BigDecimal b) {}
