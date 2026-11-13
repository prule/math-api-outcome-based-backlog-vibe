package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** BR-1: an Operand, as written, has at most 100 significant digits and a scale within ±1000. */
class OperandLimitsTest {

    private static Calculation add(String a, String b) {
        return new Calculation(Operation.ADD, new BigDecimal(a), new BigDecimal(b));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E+1000", "1E-1000", "-1E+1000"})
    void acceptsAnOperandAtTheScaleLimit_BR1(String operand) {
        assertThat(add(operand, "0").a()).isEqualByComparingTo(operand);
        assertThat(add("0", operand).b()).isEqualByComparingTo(operand);
    }

    @Test
    void acceptsAnOperandOfExactlyOneHundredDigits_BR1() {
        String hundredDigits = "9".repeat(100);
        assertThat(add(hundredDigits, "0." + hundredDigits).result().precision())
                .isEqualTo(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E+1001", "1E-1001", "1E+999999999", "1E-999999999"})
    void rejectsAnOperandPastTheScaleLimit_BR1(String operand) {
        assertThatThrownBy(() -> add(operand, "0"))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessage("Operand 'a' has a scale outside ±1000.");
        assertThatThrownBy(() -> add("0", operand))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessageContaining("'b'");
    }

    @Test
    void rejectsAnOperandOfOneHundredAndOneDigits_BR1() {
        assertThatThrownBy(() -> add("9".repeat(101), "0"))
                .isInstanceOf(OperandOutOfRangeException.class)
                .hasMessage("Operand 'a' has more than 100 significant digits.");
        assertThatThrownBy(() -> add("0." + "9".repeat(101), "0")).isInstanceOf(OperandOutOfRangeException.class);
    }

    @Test
    void judgesTheOperandAsWrittenNotItsValue_BR1() {
        // 1 followed by 100 zeros is 101 digits as written, though 1E+100 is the same value and is accepted
        assertThatThrownBy(() -> add("1" + "0".repeat(100), "0")).isInstanceOf(OperandOutOfRangeException.class);
        assertThat(add("1E+100", "0").result()).isEqualByComparingTo("1E+100");
    }

    @Test
    void neverEchoesTheOperandValue_BR1() {
        assertThatThrownBy(() -> add("1E+1001", "0"))
                .message()
                .doesNotContain("1001")
                .doesNotContain("1E");
    }

    @Test
    void rejectsHugeOperandsBeforeAnyArithmetic_BR1() {
        assertTimeoutPreemptively(
                Duration.ofMillis(200),
                () -> assertThatThrownBy(() -> new Calculation(
                                Operation.MULTIPLY, new BigDecimal("1e999999999"), new BigDecimal("1e999999999")))
                        .isInstanceOf(OperandOutOfRangeException.class));
    }

    @Test
    void checksOperandLimitsBeforeDivisionByZero() {
        assertThatThrownBy(() -> new Calculation(Operation.DIVIDE, new BigDecimal("1E+1001"), BigDecimal.ZERO))
                .isInstanceOf(OperandOutOfRangeException.class);
    }
}
