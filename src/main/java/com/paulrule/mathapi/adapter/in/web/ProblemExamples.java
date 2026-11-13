package com.paulrule.mathapi.adapter.in.web;

/**
 * Problem Details examples for the API docs, one per PRD §3.2 type. The unknown-operation example lists the
 * Operations, so it is generated from the domain in config.OpenApiConfig instead.
 */
final class ProblemExamples {

    static final String INVALID_REQUEST = """
            {"type": "/problems/invalid-request", "title": "Invalid request", "status": 400,
             "detail": "Missing or empty field(s): 'b'.", "instance": "/api/v1/calculations"}""";

    static final String OPERAND_OUT_OF_RANGE = """
            {"type": "/problems/operand-out-of-range", "title": "Operand out of range", "status": 400,
             "detail": "Operand 'a' has a scale outside ±1000.", "instance": "/api/v1/calculations"}""";

    static final String DIVISION_BY_ZERO = """
            {"type": "/problems/division-by-zero", "title": "Division by zero", "status": 422,
             "detail": "Cannot divide by zero.", "instance": "/api/v1/calculations"}""";

    static final String UNSUPPORTED_MEDIA_TYPE = """
            {"title": "Unsupported Media Type", "status": 415,
             "detail": "Content-Type 'text/plain' is not supported.", "instance": "/api/v1/calculations"}""";

    static final String UNEXPECTED = """
            {"title": "Internal Server Error", "status": 500, "detail": "Unexpected error.",
             "instance": "/api/v1/calculations"}""";

    private ProblemExamples() {}
}
