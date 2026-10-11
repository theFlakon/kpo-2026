package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.InvalidTechnicalConditionException;
import domain.inspection.InspectionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

/**
 * Решение техосмотра по правилам: тормоза и порог состояния.
 */
@DisplayName("RuleBasedServiceCenter")
class RuleBasedServiceCenterTest {

    private final RuleBasedServiceCenter serviceCenter =
            new RuleBasedServiceCenter(70);

    @ParameterizedTest(name = "состояние {0}%, тормоза исправны: {1} → принят: {2}")
    @CsvSource({
        "70,  true,  true",
        "69,  true,  false",
        "100, false, false"
    })
    @DisplayName("решение по состоянию и тормозам")
    void decidesByConditionAndBrakes(int condition, boolean brakesOk,
            boolean expected) {
        InspectionResult result = serviceCenter.inspect(TestData.intake()
                .technicalCondition(condition)
                .brakesOk(brakesOk)
                .build());

        assertEquals(expected, result.passed());
    }

    @Test
    @DisplayName("при нескольких нарушениях перечисляет все")
    void listsAllProblems() {
        String reason = serviceCenter.inspect(TestData.intake()
                .technicalCondition(40)
                .brakesOk(false)
                .build()).reason();

        assertTrue(reason.contains("тормоза"));
        assertTrue(reason.contains("40%"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    @DisplayName("порог вне 0–100 % отклоняется")
    void rejectsThresholdOutOfRange(int threshold) {
        assertThrows(InvalidTechnicalConditionException.class,
                () -> new RuleBasedServiceCenter(threshold));
    }

    @Test
    @DisplayName("одобрение несёт стандартную причину")
    void passCarriesDefaultReason() {
        assertEquals("Техосмотр пройден",
                serviceCenter.inspect(TestData.intake().build()).reason());
    }

    @Test
    @DisplayName("порог 0 % проверяет только тормоза")
    void zeroThresholdChecksOnlyBrakes() {
        RuleBasedServiceCenter lenient = new RuleBasedServiceCenter(0);

        assertTrue(lenient.inspect(TestData.intake().technicalCondition(0)
                .build()).passed());
        assertEquals("тормоза неисправны", lenient.inspect(TestData.intake()
                .brakesOk(false).build()).reason());
    }

    @Test
    @DisplayName("null вместо заявки — ошибка программиста")
    void rejectsNullRequest() {
        assertThrows(NullPointerException.class,
                () -> serviceCenter.inspect(null));
    }
}
