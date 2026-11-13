package com.paulrule.mathapi.domain;

/** The requested Operation name is not one of {@link Operation}'s constants. */
public class UnknownOperationException extends RuntimeException {

    UnknownOperationException(String supportedNames) {
        super("Operation must be one of: " + supportedNames + ".");
    }
}
