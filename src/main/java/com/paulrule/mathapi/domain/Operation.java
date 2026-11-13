package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
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
    };

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

    static String supportedNames() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
