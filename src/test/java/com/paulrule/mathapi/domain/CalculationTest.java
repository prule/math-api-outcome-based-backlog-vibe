package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculationTest {

    private static String result(Operation operation, String a, String b) {
        return new Calculation(operation, new BigDecimal(a), new BigDecimal(b))
                .result()
                .toPlainString();
    }

    @Test
    void addsPointOneAndPointTwoToExactlyPointThree() {
        assertThat(result(Operation.ADD, "0.1", "0.2")).isEqualTo("0.3");
    }

    @ParameterizedTest(name = "{0} {1} {2} = {3}")
    @CsvSource({
        // ADD: negatives
        "ADD, -1.5, -2.5, -4",
        "ADD, -10, 3, -7",
        // ADD: large precision, no digit lost
        "ADD, 1234567890123456789012345678901234567890.123456789, 0.000000001, 1234567890123456789012345678901234567890.12345679",
        // SUBTRACT: result zero, whatever the scale
        "SUBTRACT, 1.50, 1.5, 0",
        "SUBTRACT, 7, 7, 0",
        // SUBTRACT: negative result
        "SUBTRACT, 2, 5, -3",
        "SUBTRACT, -0.1, 0.2, -0.3",
        // MULTIPLY: by zero
        "MULTIPLY, 1.25, 0.00, 0",
        "MULTIPLY, -3, 0, 0",
        // MULTIPLY: sign rules
        "MULTIPLY, -2, 3, -6",
        "MULTIPLY, 2, -3, -6",
        "MULTIPLY, -2, -3, 6",
        // MULTIPLY: scale growth
        "MULTIPLY, 1.1, 1.1, 1.21",
        "MULTIPLY, 0.001, 0.001, 0.000001",
    })
    void appliesTheOperationExactly(Operation operation, String a, String b, String expected) {
        assertThat(result(operation, a, b)).isEqualTo(expected);
    }

    @Test
    void stripsTrailingZerosFromTheResult_BR4() {
        assertThat(result(Operation.ADD, "1.10", "1.20")).isEqualTo("2.3");
        assertThat(new Calculation(Operation.MULTIPLY, new BigDecimal("1E+2"), new BigDecimal("10"))
                        .result()
                        .toPlainString())
                .isEqualTo("1000");
    }

    @Test
    void findsAnOperationByItsExactName() {
        assertThat(Operation.named("SUBTRACT")).isEqualTo(Operation.SUBTRACT);
    }

    @Test
    void rejectsANameInTheWrongCaseOrUnknown() {
        assertThatThrownBy(() -> Operation.named("add"))
                .isInstanceOf(UnknownOperationException.class)
                .hasMessage("Operation must be one of: ADD, SUBTRACT, MULTIPLY.");
        assertThatThrownBy(() -> Operation.named("FROBNICATE")).isInstanceOf(UnknownOperationException.class);
    }
}
