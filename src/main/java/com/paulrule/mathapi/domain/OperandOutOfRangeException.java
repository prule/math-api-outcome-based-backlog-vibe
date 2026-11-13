package com.paulrule.mathapi.domain;

/** BR-1: an Operand, as written, has too many significant digits or a scale outside the bounds. */
public class OperandOutOfRangeException extends RuntimeException {

    // names the Operand and the limit, never the value, which may be huge (PRD §3.2)
    OperandOutOfRangeException(String operandName, String brokenLimit) {
        super("Operand '" + operandName + "' " + brokenLimit + ".");
    }
}
