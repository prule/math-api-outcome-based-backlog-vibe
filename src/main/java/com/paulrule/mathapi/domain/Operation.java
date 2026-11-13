package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.stream.Collectors;

/** The closed set of arithmetic Operations (DOMAIN-MODEL.md §3). */
public enum Operation {
    ADD {
        @Override
        BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }
    },
    SUBTRACT {
        @Override
        BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }
    },
    MULTIPLY {
        @Override
        BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.multiply(b);
        }
    },
    DIVIDE {
        @Override
        BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) {
                throw new DivisionByZeroException(); // BR-2: every zero form has signum 0
            }
            return a.divide(b, ROUNDING);
        }
    };

    /** BR-3: 34 significant digits, half to even, for every Operation that rounds. */
    public static final MathContext ROUNDING = MathContext.DECIMAL128;

    abstract BigDecimal apply(BigDecimal a, BigDecimal b);

    /** Exact, case-sensitive lookup: {@code ADD}, not {@code add}. */
    public static Operation named(String name) {
        for (Operation operation : values()) {
            if (operation.name().equals(name)) {
                return operation;
            }
        }
        throw new UnknownOperationException(supportedNames());
    }

    /** The Operation names in declaration order, comma separated. */
    public static String supportedNames() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
