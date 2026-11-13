package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OperationTest {

    @Test
    void findsAnOperationByItsExactName() {
        assertThat(Operation.named("SUBTRACT")).isEqualTo(Operation.SUBTRACT);
    }

    @Test
    void rejectsANameInTheWrongCaseOrUnknown() {
        assertThatThrownBy(() -> Operation.named("add"))
                .isInstanceOf(UnknownOperationException.class)
                .hasMessage("Operation must be one of: ADD, SUBTRACT, MULTIPLY, DIVIDE.");
        assertThatThrownBy(() -> Operation.named("FROBNICATE")).isInstanceOf(UnknownOperationException.class);
    }
}
