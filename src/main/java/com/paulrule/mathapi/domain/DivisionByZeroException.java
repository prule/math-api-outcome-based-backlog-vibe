package com.paulrule.mathapi.domain;

/** BR-2: the divisor is zero, in any representation. */
public class DivisionByZeroException extends RuntimeException {

    DivisionByZeroException() {
        super("Cannot divide by zero.");
    }
}
