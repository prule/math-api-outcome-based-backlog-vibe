package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.Calculation;
import io.swagger.v3.oas.annotations.media.Schema;

/** Every decimal is a plain-notation string so clients never lose precision (PRD §3.2). */
@Schema(requiredProperties = {"operation", "a", "b", "result"})
record CalculationResponse(
        @Schema(example = "DIVIDE") String operation,

        @Schema(description = "Operand a as received, in plain notation with its given scale.", example = "1")
        String a,

        @Schema(description = "Operand b as received, in plain notation with its given scale.", example = "3")
        String b,

        @Schema(
                description = "The Result in plain notation, trailing zeros stripped (BR-4). DIVIDE rounds to"
                        + " 34 significant digits, half to even (BR-3); the others are exact.",
                example = "0.3333333333333333333333333333333333")
        String result) {

    static CalculationResponse from(Calculation calculation) {
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
