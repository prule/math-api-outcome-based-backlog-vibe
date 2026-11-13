package com.paulrule.mathapi.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** PRD §3.3 and STANDARDS §4: a developer can integrate from the generated docs alone. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiDocumentationTest {

    private static final String CALCULATIONS = "$.paths['/api/v1/calculations'].post";
    private static final String REQUEST = "$.components.schemas.CalculationRequest.properties";
    private static final String RESPONSE = "$.components.schemas.CalculationResponse.properties";

    @Autowired
    MockMvc mvc;

    private DocumentContext spec;

    @BeforeEach
    void readSpec() throws Exception {
        String json = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        spec = JsonPath.parse(json);
    }

    private <T> T read(String path) {
        return spec.read(path);
    }

    @Test
    void servesAnOpenApi31SpecAndAnInteractiveUi() throws Exception {
        assertThat(this.<String>read("$.openapi")).startsWith("3.1");
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void listsEveryOperationTheDomainSupports() {
        List<String> documented = read(REQUEST + ".operation.enum");
        assertThat(documented)
                .containsExactly(
                        Arrays.stream(Operation.values()).map(Enum::name).toArray(String[]::new));
    }

    @Test
    void documentsOperandsAsNumberOrString() {
        for (String operand : List.of("a", "b")) {
            assertThat(this.<List<String>>read(REQUEST + "." + operand + ".type"))
                    .as(operand)
                    .containsExactlyInAnyOrder("number", "string");
        }
        assertThat(this.<List<String>>read("$.components.schemas.CalculationRequest.required"))
                .containsExactlyInAnyOrder("operation", "a", "b");
    }

    @Test
    void documentsTheLimitsAndRoundingTheDomainEnforces() {
        assertThat(this.<String>read(REQUEST + ".a.description"))
                .contains("At most " + Calculation.MAX_PRECISION + " significant digits")
                .contains("±" + Calculation.MAX_SCALE);
        assertThat(this.<String>read(RESPONSE + ".result.description"))
                .contains(Operation.ROUNDING.getPrecision() + " significant digits");
    }

    @Test
    void documentsTheRequestLimits() {
        assertThat(this.<String>read(CALCULATIONS + ".responses['400'].description"))
                .contains("at most 16384 bytes")
                .contains("at most 1000 characters")
                .contains("at most 500 levels deep");
    }

    @Test
    void documentsEveryResponseValueAsAString() {
        for (String field : List.of("operation", "a", "b", "result")) {
            assertThat(this.<String>read(RESPONSE + "." + field + ".type"))
                    .as(field)
                    .isEqualTo("string");
        }
        assertThat(this.<List<String>>read("$.components.schemas.CalculationResponse.required"))
                .containsExactlyInAnyOrder("operation", "a", "b", "result");
        assertThat(this.<String>read(CALCULATIONS + ".responses['200'].content['application/json'].schema.$ref"))
                .endsWith("/CalculationResponse");
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
        "400, invalid-request",
        "400, unknown-operation",
        "400, operand-out-of-range",
        "422, division-by-zero",
    })
    void documentsEveryProblemTypeWithAnExample(int status, String type) {
        Map<String, Object> example = read(CALCULATIONS + ".responses['" + status
                + "'].content['application/problem+json'].examples['" + type + "'].value");
        assertThat(example)
                .containsEntry("type", "/problems/" + type)
                .containsEntry("status", status)
                .containsKeys("title", "detail");
    }

    @Test
    void describesProblemDetailsAsTheyAreSent() {
        Map<String, Map<String, Object>> problem = read("$.components.schemas.ProblemDetail.properties");
        // Spring writes extra members at the top level, never under a "properties" key
        assertThat(problem).containsOnlyKeys("type", "title", "status", "detail", "instance");
        // our type URIs are relative ("/problems/..."), which "uri" would reject
        assertThat(problem.get("type")).containsEntry("format", "uri-reference");
        assertThat(problem.get("instance")).containsEntry("format", "uri-reference");
    }

    @ParameterizedTest
    @CsvSource({"406", "415", "500"})
    void documentsFrameworkErrorsAsProblemDetails(String status) {
        assertThat(this.<Map<String, Object>>read(CALCULATIONS + ".responses['" + status + "'].content"))
                .containsOnlyKeys("application/problem+json");
    }

    /** Each example is exactly what the API sends for a request that triggers it, so the docs cannot drift. */
    @ParameterizedTest(name = "{1}")
    @CsvSource(
            delimiter = '|',
            value = {
                "400 | invalid-request        | application/json | {\"operation\":\"ADD\",\"a\":1}",
                "400 | unknown-operation      | application/json | {\"operation\":\"FROBNICATE\",\"a\":1,\"b\":2}",
                "400 | operand-out-of-range   | application/json | {\"operation\":\"ADD\",\"a\":\"1E+1001\",\"b\":2}",
                "422 | division-by-zero       | application/json | {\"operation\":\"DIVIDE\",\"a\":1,\"b\":0}",
                "415 | unsupported-media-type | text/plain       | 1 + 2",
                "406 | not-acceptable         | application/json | {\"operation\":\"ADD\",\"a\":1,\"b\":2}",
            })
    void showsTheRealResponseInEachExample(int status, String example, String contentType, String body)
            throws Exception {
        // a 406 is what a client gets when it asks for a representation this endpoint never produces
        String accept = status == 406 ? MediaType.TEXT_PLAIN_VALUE : MediaType.ALL_VALUE;
        String actual = mvc.perform(post("/api/v1/calculations")
                        .contentType(contentType)
                        .accept(accept)
                        .content(body))
                .andExpect(status().is(status))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Map<String, Object> documented = read(CALCULATIONS + ".responses['" + status
                + "'].content['application/problem+json'].examples['" + example + "'].value");
        assertThat(documented).isEqualTo(JsonPath.read(actual, "$"));
    }
}
