package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.InvalidSimplicityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

/**
 * Правило из задания: простота не ниже порога.
 */
@DisplayName("SimplicityThresholdPolicy")
class SimplicityThresholdPolicyTest {

    private final SimplicityThresholdPolicy policy = new SimplicityThresholdPolicy(
            SimplicityThresholdPolicy.DEFAULT_THRESHOLD);

    // Новый граничный случай добавляется одной строкой таблицы.
    @ParameterizedTest(name = "простота {0} → подходит новичку: {1}")
    @CsvSource({
        "1,  false",
        "5,  false",
        "6,  true",
        "10, true"
    })
    @DisplayName("порог 6 включительно")
    void appliesThresholdInclusively(int simplicity, boolean expected) {
        assertEquals(expected, policy.isSuitableForBeginner(
                TestData.bicycle(1, simplicity)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    @DisplayName("порог вне шкалы простоты отклоняется")
    void rejectsThresholdOutsideScale(int threshold) {
        assertThrows(InvalidSimplicityException.class,
                () -> new SimplicityThresholdPolicy(threshold));
    }

    @Test
    @DisplayName("описывает правило для отчёта")
    void describesRule() {
        assertEquals("простота ≥ 6", policy.description());
    }

    @Test
    @DisplayName("порог 1 пропускает всё, порог 10 — только самое простое")
    void extremeThresholds() {
        assertTrue(new SimplicityThresholdPolicy(1)
                .isSuitableForBeginner(TestData.bicycle(1, 1)));
        assertFalse(new SimplicityThresholdPolicy(10)
                .isSuitableForBeginner(TestData.bicycle(1, 9)));
    }

    @Test
    @DisplayName("описание отражает заданный порог")
    void descriptionFollowsThreshold() {
        assertEquals("простота ≥ 3",
                new SimplicityThresholdPolicy(3).description());
    }
}
