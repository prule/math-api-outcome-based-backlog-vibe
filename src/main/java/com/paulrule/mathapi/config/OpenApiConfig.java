package com.paulrule.mathapi.config;

import com.paulrule.mathapi.domain.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

/** Puts the values that come from the domain into the API docs, so a new Operation needs no doc edit (STANDARDS §4). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer domainValues() {
        return openApi -> {
            openApi.info(new Info()
                    .title("Math API")
                    .version("v1")
                    .description("Exact decimal arithmetic over HTTP. Errors are RFC 9457 Problem Details."));

            // springdoc reads ProblemDetail's getters; Spring's Jackson mixin sends extras at the top level and the
            // PRD's type URIs are relative, so describe the body as it is actually sent
            Map<String, Schema> problem =
                    openApi.getComponents().getSchemas().get("ProblemDetail").getProperties();
            problem.remove("properties");
            problem.get("type").setFormat("uri-reference");
            problem.get("instance").setFormat("uri-reference");

            List<String> names =
                    Arrays.stream(Operation.values()).map(Enum::name).toList();
            @SuppressWarnings("unchecked")
            Schema<Object> operation = (Schema<Object>) openApi.getComponents()
                    .getSchemas()
                    .get("CalculationRequest")
                    .getProperties()
                    .get("operation");
            operation.setEnum(List.copyOf(names));

            // the same detail ApiExceptionHandler returns; ApiDocumentationTest fails if the two drift
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
