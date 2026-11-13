package com.paulrule.mathapi.adapter.in.web;

import java.util.Optional;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.core.exc.StreamConstraintsException;

/**
 * Tells a client which request limit it broke (PRD §3.2). The limits themselves are Jackson's stream read
 * constraints, set in application.yaml, so the parser stops before a huge body or value costs anything.
 */
class RequestLimits {

    private final long maxBodyBytes;
    private final int maxStringLength;
    private final int maxNumberLength;
    private final int maxNestingDepth;

    RequestLimits(long maxBodyBytes, int maxStringLength, int maxNumberLength, int maxNestingDepth) {
        this.maxBodyBytes = maxBodyBytes;
        this.maxStringLength = maxStringLength;
        this.maxNumberLength = maxNumberLength;
        this.maxNestingDepth = maxNestingDepth;
    }

    /** The limit {@code ex} reports, described for the client; empty if it is not a limit failure. */
    Optional<String> brokenBy(HttpMessageNotReadableException ex) {
        if (!(ex.getMostSpecificCause() instanceof StreamConstraintsException limit)) {
            return Optional.empty();
        }
        // Jackson has one exception type for every limit; its message is the only way to tell them apart.
        // HostileRequestTest fails if a Jackson upgrade changes these wordings.
        String message = limit.getOriginalMessage();
        if (message.startsWith("Document length")) {
            return Optional.of("The request body is larger than " + maxBodyBytes + " bytes.");
        }
        if (message.startsWith("Number value length")) {
            return Optional.of("A JSON number in the request is longer than " + maxNumberLength + " characters.");
        }
        if (message.startsWith("String value length")) {
            return Optional.of("A JSON string in the request is longer than " + maxStringLength + " characters.");
        }
        if (message.startsWith("Document nesting depth")) {
            return Optional.of("The request body is nested more than " + maxNestingDepth + " levels deep.");
        }
        return Optional.of("The request breaks a size limit.");
    }
}
