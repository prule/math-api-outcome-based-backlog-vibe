package com.paulrule.mathapi.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paulrule.mathapi.config.UseCaseConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CalculationController.class)
@Import(UseCaseConfig.class)
class CalculationControllerTest {

    @Autowired
    MockMvc mvc;

    private ResultActions postJson(String body) throws Exception {
        return mvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions expectProblem(ResultActions result, int status, String type) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void addsJsonNumbersExactly() throws Exception {
        postJson("{\"operation\":\"ADD\",\"a\":0.1,\"b\":0.2}")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("ADD"))
                .andExpect(jsonPath("$.a").value("0.1"))
                .andExpect(jsonPath("$.b").value("0.2"))
                .andExpect(jsonPath("$.result").value("0.3"));
    }

    @Test
    void acceptsNumericStringsAndEchoesOperandsWithTheirScaleInPlainNotation() throws Exception {
        postJson("{\"operation\":\"MULTIPLY\",\"a\":\"1.50\",\"b\":1e2}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("1.50"))
                .andExpect(jsonPath("$.b").value("100"))
                .andExpect(jsonPath("$.result").value("150"));
    }

    @Test
    void returnsTheResultInPlainNotation_BR4() throws Exception {
        postJson("{\"operation\":\"SUBTRACT\",\"a\":\"1E+3\",\"b\":\"0\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("1000"))
                .andExpect(jsonPath("$.result").value("1000"));
    }

    @Test
    void dividesToThirtyFourSignificantDigits_BR3() throws Exception {
        postJson("{\"operation\":\"DIVIDE\",\"a\":1,\"b\":\"3\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0." + "3".repeat(34)));
    }

    @Test
    void dividesExactlyWhenTheResultFits() throws Exception {
        postJson("{\"operation\":\"DIVIDE\",\"a\":1,\"b\":4}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0.25"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "\"0\"", "0.00", "\"0.00\"", "-0", "\"-0\""})
    void rejectsDivisionByAnyZeroAsUnprocessable_BR2(String zero) throws Exception {
        expectProblem(
                postJson("{\"operation\":\"DIVIDE\",\"a\":1,\"b\":" + zero + "}"), 422, "/problems/division-by-zero");
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        expectProblem(postJson("{\"operation\":\"ADD\","), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsANonNumericOperand() throws Exception {
        expectProblem(postJson("{\"operation\":\"ADD\",\"a\":\"abc\",\"b\":1}"), 400, "/problems/invalid-request");
        expectProblem(postJson("{\"operation\":\"ADD\",\"a\":true,\"b\":1}"), 400, "/problems/invalid-request");
        expectProblem(postJson("{\"operation\":\"ADD\",\"a\":{},\"b\":1}"), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsAMissingOrEmptyField() throws Exception {
        expectProblem(postJson("{\"operation\":\"ADD\",\"a\":1}"), 400, "/problems/invalid-request");
        expectProblem(postJson("{\"operation\":\"ADD\",\"a\":\"\",\"b\":1}"), 400, "/problems/invalid-request");
        expectProblem(postJson("{\"a\":1,\"b\":2}"), 400, "/problems/invalid-request");
        expectProblem(postJson("{}"), 400, "/problems/invalid-request")
                .andExpect(jsonPath("$.detail").value("Missing or empty field(s): 'a', 'b', 'operation'."));
    }

    @Test
    void reportsAnInvalidRequestBeforeAnUnknownOperation() throws Exception {
        expectProblem(postJson("{\"operation\":\"FROBNICATE\",\"a\":1}"), 400, "/problems/invalid-request");
    }

    @Test
    void rejectsAnUnknownOperation() throws Exception {
        expectProblem(postJson("{\"operation\":\"FROBNICATE\",\"a\":1,\"b\":2}"), 400, "/problems/unknown-operation")
                .andExpect(jsonPath("$.detail").value("Operation must be one of: ADD, SUBTRACT, MULTIPLY, DIVIDE."));
    }

    @Test
    void treatsOperationNamesAsCaseSensitive() throws Exception {
        expectProblem(postJson("{\"operation\":\"add\",\"a\":1,\"b\":2}"), 400, "/problems/unknown-operation");
    }

    @Test
    void answersAWrongContentTypeWithProblemDetails() throws Exception {
        mvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("1+2"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void answersAnUnexpectedFailureWithProblemDetailsThatHideTheCause() throws Exception {
        // the product's scale overflows int; until item 4's operand limits this is an unforeseen ArithmeticException
        postJson("{\"operation\":\"MULTIPLY\",\"a\":\"1e-1500000000\",\"b\":\"1e-1500000000\"}")
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Unexpected error."));
    }
}
