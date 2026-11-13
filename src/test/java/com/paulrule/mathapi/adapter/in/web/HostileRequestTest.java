package com.paulrule.mathapi.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.config.UseCaseConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Hostile requests are rejected by the limit they break (PRD §3.2 request limits, BR-1), before any arithmetic. */
@WebMvcTest(CalculationController.class)
@Import(UseCaseConfig.class)
class HostileRequestTest {

    private static final int MAX_BODY = 16_384;

    @Autowired
    MockMvc mvc;

    private ResultActions postJson(String body) throws Exception {
        return mvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String calculation(String a, String b) {
        return "{\"operation\":\"MULTIPLY\",\"a\":" + a + ",\"b\":" + b + "}";
    }

    /** Pads a valid body with trailing whitespace to exactly {@code length} characters. */
    private static String padTo(String body, int length) {
        return body + " ".repeat(length - body.length());
    }

    private void expectProblem(ResultActions result, int status, String type, String detailFragment) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.detail").value(Matchers.containsString(detailFragment)));
    }

    @Test
    void rejectsHugeOperandsAsOutOfRangeImmediately_BR1() throws Exception {
        expectProblem(
                postJson(calculation("\"1e999999999\"", "1e999999999")), 400, "/problems/operand-out-of-range", "'a'");
    }

    @Test
    void namesTheOperandOutOfRangeButNeverEchoesIt_BR1() throws Exception {
        postJson(calculation("1", "\"1E-1001\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/problems/operand-out-of-range"))
                .andExpect(jsonPath("$.detail").value(Matchers.containsString("'b'")))
                .andExpect(jsonPath("$.detail").value(Matchers.not(Matchers.containsString("1001"))));
    }

    @Test
    void acceptsOperandsAtTheLimits_BR1() throws Exception {
        postJson(calculation("\"1E+1000\"", "\"" + "9".repeat(100) + "\"")).andExpect(status().isOk());
        postJson(calculation("1E-1000", "\"-1E+1000\"")).andExpect(status().isOk());
    }

    @Test
    void saysWhichOperandLimitWasBroken_BR1() throws Exception {
        expectProblem(
                postJson(calculation("\"" + "9".repeat(101) + "\"", "1")),
                400,
                "/problems/operand-out-of-range",
                "Operand 'a' has more than 100 significant digits.");
        expectProblem(
                postJson(calculation("1", "1E+1001")),
                400,
                "/problems/operand-out-of-range",
                "Operand 'b' has a scale outside ±1000.");
    }

    @Test
    void reportsAnUnknownOperationBeforeAnOperandOutOfRange() throws Exception {
        expectProblem(
                postJson("{\"operation\":\"FROBNICATE\",\"a\":\"1E+1001\",\"b\":1}"),
                400,
                "/problems/unknown-operation",
                "Operation must be one of");
    }

    @Test
    void reportsAnOperandOutOfRangeBeforeDivisionByZero() throws Exception {
        expectProblem(
                postJson("{\"operation\":\"DIVIDE\",\"a\":\"1E+1001\",\"b\":0}"),
                400,
                "/problems/operand-out-of-range",
                "'a'");
    }

    @Test
    void acceptsABodyOfExactlyTheMaximumSize() throws Exception {
        postJson(padTo(calculation("1", "2"), MAX_BODY)).andExpect(status().isOk());
    }

    @Test
    void rejectsABodyOneCharacterOverTheMaximumSize() throws Exception {
        expectProblem(
                postJson(padTo(calculation("1", "2"), MAX_BODY + 1)),
                400,
                "/problems/invalid-request",
                "larger than 16384 bytes");
    }

    @Test
    void rejectsAJsonNumberOverOneThousandCharactersBeforeParsingIt() throws Exception {
        expectProblem(
                postJson(calculation("1" + "0".repeat(1000), "1")),
                400,
                "/problems/invalid-request",
                "JSON number in the request is longer than 1000 characters");
    }

    @Test
    void rejectsAJsonStringOverOneThousandCharactersBeforeParsingIt() throws Exception {
        expectProblem(
                postJson(calculation("\"" + "1".repeat(1001) + "\"", "1")),
                400,
                "/problems/invalid-request",
                "JSON string in the request is longer than 1000 characters");
    }

    @Test
    void passesValuesOfExactlyOneThousandCharactersOnToTheOperandLimits() throws Exception {
        expectProblem(postJson(calculation("1" + "0".repeat(999), "1")), 400, "/problems/operand-out-of-range", "'a'");
        expectProblem(
                postJson(calculation("1", "\"" + "1".repeat(1000) + "\"")),
                400,
                "/problems/operand-out-of-range",
                "'b'");
    }

    @Test
    void rejectsADeeplyNestedBodyAsTooDeepNotTooLarge() throws Exception {
        // an ignored field is skipped token by token, so this is where deep nesting would cost parser work
        String nested = "[".repeat(600) + "]".repeat(600);
        expectProblem(
                postJson("{\"operation\":\"ADD\",\"a\":1,\"b\":1,\"ignored\":" + nested + "}"),
                400,
                "/problems/invalid-request",
                "nested more than 500 levels deep");
    }

    @Test
    void acceptsNestingUpToTheLimitInAnIgnoredField() throws Exception {
        String nested = "[".repeat(499) + "]".repeat(499);
        postJson("{\"operation\":\"ADD\",\"a\":1,\"b\":1,\"ignored\":" + nested + "}")
                .andExpect(status().isOk());
    }
}
