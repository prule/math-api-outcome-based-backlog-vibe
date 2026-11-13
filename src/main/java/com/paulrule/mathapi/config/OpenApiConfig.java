package com.paulrule.mathapi.config;

import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer apiInfo() {
        return openApi -> openApi.info(new Info()
                .title("Math API")
                .version("v1")
                .description("Exact decimal arithmetic over HTTP. Errors are RFC 9457 Problem Details."));
    }

    /**
     * springdoc describes ProblemDetail from its getters, but Spring's Jackson mixin sends extra members at the top
     * level (never under "properties"), and the PRD's type URIs are relative. Describe the body as it is sent.
     */
    @Bean
    OpenApiCustomizer problemDetailsAsSent() {
        return openApi -> {
            Map<String, Schema> problem =
                    openApi.getComponents().getSchemas().get("ProblemDetail").getProperties();
            problem.remove("properties");
            problem.get("type").setFormat("uri-reference");
            problem.get("instance").setFormat("uri-reference");
        };
    }

    /** The PRD §3.2 request limits, read from the same properties that set the parser's limits. */
    @Bean
    OpenApiCustomizer requestLimits(
            @Value("${spring.jackson.factory.constraints.read.max-document-length}") long maxBodyBytes,
            @Value("${spring.jackson.factory.constraints.read.max-string-length}") int maxStringLength,
            @Value("${spring.jackson.factory.constraints.read.max-number-length}") int maxNumberLength,
            @Value("${spring.jackson.factory.constraints.read.max-nesting-depth}") int maxNestingDepth) {
        return openApi -> {
            var badRequest = openApi.getPaths()
                    .get("/api/v1/calculations")
                    .getPost()
                    .getResponses()
                    .get("400");
            badRequest.setDescription(badRequest.getDescription() + " Request limits: a body of at most "
                    + maxBodyBytes + " bytes, JSON strings of at most " + maxStringLength
                    + " characters, JSON numbers of at most " + maxNumberLength + " characters, and nesting at most "
                    + maxNestingDepth + " levels deep; the detail names the limit broken.");
        };
    }

    /** Values that come from the domain, so a new Operation or a changed rule needs no doc edit (STANDARDS §4). */
    @Bean
    @SuppressWarnings("unchecked")
    OpenApiCustomizer domainValues() {
        return openApi -> {
            Map<String, Schema> request = openApi.getComponents()
                    .getSchemas()
                    .get("CalculationRequest")
                    .getProperties();
            request.get("operation")
                    .setEnum(Arrays.stream(Operation.values()).map(Enum::name).toList());
            String operand = "A decimal Operand, as a JSON number or a numeric string (send a string to keep every"
                    + " digit through clients that parse JSON numbers as floating point). At most "
                    + Calculation.MAX_PRECISION + " significant digits and a scale within ±" + Calculation.MAX_SCALE
                    + ", as written (BR-1).";
            request.get("a").setDescription(operand);
            request.get("b").setDescription(operand);

            Map<String, Schema> response = openApi.getComponents()
                    .getSchemas()
                    .get("CalculationResponse")
                    .getProperties();
            response.get("result")
                    .setDescription("The Result in plain notation, trailing zeros stripped (BR-4). DIVIDE rounds to "
                            + Operation.ROUNDING.getPrecision() + " significant digits, "
                            + Operation.ROUNDING.getRoundingMode() + " (BR-3); the others are exact.");

            // the detail ApiExceptionHandler sends; ApiDocumentationTest fails if the two drift
            Map<String, Object> unknownOperation = new LinkedHashMap<>();
            unknownOperation.put("type", "/problems/unknown-operation");
            unknownOperation.put("title", "Unknown operation");
            unknownOperation.put("status", 400);
            unknownOperation.put("detail", "Operation must be one of: " + Operation.supportedNames() + ".");
            unknownOperation.put("instance", "/api/v1/calculations");
            openApi.getPaths()
                    .get("/api/v1/calculations")
                    .getPost()
                    .getResponses()
                    .get("400")
                    .getContent()
                    .get(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .addExamples("unknown-operation", new Example().value(unknownOperation));
        };
    }
}
