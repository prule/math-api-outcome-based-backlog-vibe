package com.paulrule.mathapi.application;

import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import java.math.BigDecimal;

/** Use case: perform one Calculation and return it with its Result. */
public class PerformCalculation {

    public Calculation perform(String operationName, BigDecimal a, BigDecimal b) {
        return new Calculation(Operation.named(operationName), a, b);
    }
}
