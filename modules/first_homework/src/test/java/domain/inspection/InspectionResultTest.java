package domain.inspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.InvalidInspectionResultException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Решение техосмотра всегда объяснимо.
 */
@DisplayName("InspectionResult")
class InspectionResultTest {

    @Test
    @DisplayName("одобрение получает стандартную причину")
    void passHasReason() {
        InspectionResult result = InspectionResult.pass();

        assertTrue(result.passed());
        assertFalse(result.reason().isBlank());
    }

    @Test
    @DisplayName("отказ хранит переданную причину")
    void failKeepsReason() {
        InspectionResult result = InspectionResult.fail("тормоза");

        assertFalse(result.passed());
        assertEquals("тормоза", result.reason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("отказ без причины не создаётся")
    void rejectsBlankReason(String reason) {
        assertThrows(InvalidInspectionResultException.class,
                () -> InspectionResult.fail(reason));
    }

    @Test
    @DisplayName("инвариант не обходится каноническим конструктором записи")
    void canonicalConstructorAlsoChecks() {
        assertThrows(InvalidInspectionResultException.class,
                () -> new InspectionResult(false, null));
    }

    @Test
    @DisplayName("одобрение с причиной по умолчанию «Техосмотр пройден»")
    void passUsesDefaultReason() {
        assertEquals("Техосмотр пройден", InspectionResult.pass().reason());
    }
}
