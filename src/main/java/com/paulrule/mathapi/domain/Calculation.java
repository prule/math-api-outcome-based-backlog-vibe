package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** One Operation applied to its Operands. */
public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    private static final int MAX_PRECISION = 100;
    private static final int MAX_SCALE = 1000;

    public Calculation {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        requireWithinLimits(a, "a");
        requireWithinLimits(b, "b");
    }

    /** BR-1, on the Operand as written: cheap to check, and bounds every later step and the echoed value. */
    private static void requireWithinLimits(BigDecimal operand, String name) {
        if (operand.precision() > MAX_PRECISION) {
            throw new OperandOutOfRangeException(name, "has more than " + MAX_PRECISION + " significant digits");
        }
        if (Math.abs(operand.scale()) > MAX_SCALE) {
            throw new OperandOutOfRangeException(name, "has a scale outside ±" + MAX_SCALE);
        }
    }

    /** The Result, with trailing zeros stripped (BR-4). Plain-notation rendering is the adapter's job. */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }
}
