package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.Calculation;

/** Every decimal is a plain-notation string so clients never lose precision (PRD §3.2). */
record CalculationResponse(String operation, String a, String b, String result) {

    static CalculationResponse from(Calculation calculation) {
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
