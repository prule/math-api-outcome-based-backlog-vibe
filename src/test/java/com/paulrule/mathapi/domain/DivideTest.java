package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DivideTest {

    private static String divide(String a, String b) {
        return new Calculation(Operation.DIVIDE, new BigDecimal(a), new BigDecimal(b))
                .result()
                .toPlainString();
    }

    @Test
    void dividesOneByThreeToThirtyFourSignificantDigits_BR3() {
        assertThat(divide("1", "3")).isEqualTo("0." + "3".repeat(34));
    }

    @Test
    void roundsTheThirtyFourthDigitUp_BR3() {
        assertThat(divide("2", "3")).isEqualTo("0." + "6".repeat(33) + "7");
    }

    @ParameterizedTest(name = "{0} ÷ {1} = {2}")
    @CsvSource({
        // a tie at the 35th digit rounds to the even neighbour (HALF_EVEN), not always up
        "1000000000000000000000000000000000.5, 1, 1000000000000000000000000000000000",
        "1000000000000000000000000000000001.5, 1, 1000000000000000000000000000000002",
    })
    void roundsTiesToEven_BR3(String a, String b, String expected) {
        assertThat(divide(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} ÷ {1} = {2}")
    @CsvSource({
        "1, 4, 0.25",
        "6, -4, -1.5",
        "-6, -4, 1.5",
        "10.00, 2, 5",
        "0, 5, 0",
        "1E+3, 1E-3, 1000000",
    })
    void dividesExactlyWhenTheResultFits(String a, String b, String expected) {
        assertThat(divide(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0", "0E+5"})
    void rejectsDivisionByAnyZero_BR2(String zero) {
        assertThatThrownBy(() -> divide("1", zero)).isInstanceOf(DivisionByZeroException.class);
    }
}
