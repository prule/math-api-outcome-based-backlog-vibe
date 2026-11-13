package com.paulrule.mathapi.domain;

/** BR-1: an Operand, as written, has too many significant digits or a scale outside ±1000. */
public class OperandOutOfRangeException extends RuntimeException {

    OperandOutOfRangeException(String operandName) {
        // names the Operand but never echoes its value, which may be huge (PRD §3.2)
        super("Operand '" + operandName + "' must have at most " + Calculation.MAX_PRECISION
                + " significant digits and a scale within ±" + Calculation.MAX_SCALE + ".");
    }
}
