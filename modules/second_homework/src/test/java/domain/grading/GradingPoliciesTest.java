package domain.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.rubric.Rubric;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;
import support.TestPolicies;

@DisplayName("Правила итога: стратегии и декоратор")
class GradingPoliciesTest {

    private final Rubric rubric = TestData.rubric();

    private final FinalGradeCalculator calculator = new FinalGradeCalculator();

    // Ведомость студента без экзамена: ДЗ 4/10, КР 8/20.
    private List<GradeLine> withoutExam() {
        return calculator.lines(rubric, TestData.graded(1, 4, 8, null));
    }

    private static void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "ожидалось " + expected + ", получено " + actual.toPlainString());
    }

    @Nested
    @DisplayName("стратегии")
    class Strategies {

        @Test
        @DisplayName("по всему рубрикатору: нет оценки за экзамен — это 0")
        void fullRubricCountsMissingAsZero() {
            assertNumber("2.4", new FullRubricPolicy().grade(withoutExam()));
        }

        @Test
        @DisplayName("текущая успеваемость: только выставленные, веса нормируются")
        void gradedOnlyIgnoresMissing() {
            assertNumber("4", new GradedOnlyPolicy().grade(withoutExam()));
        }

        @Test
        @DisplayName("без строк или без оценок итог — 0")
        void emptyInputGivesZero() {
            assertNumber("0", new FullRubricPolicy().grade(List.of()));
            assertNumber("0", new GradedOnlyPolicy().grade(
                    calculator.lines(rubric, TestData.student(1))));
        }

        @Test
        @DisplayName("у правил разные коды — их можно выбирать по коду")
        void codesDiffer() {
            assertNotEquals(new FullRubricPolicy().code(), new GradedOnlyPolicy().code());
            assertEquals(FullRubricPolicy.CODE, new FullRubricPolicy().code());
            assertEquals(GradedOnlyPolicy.CODE, new GradedOnlyPolicy().code());
        }
    }

    @Nested
    @DisplayName("декоратор округления")
    class Rounding {

        @ParameterizedTest(name = "{0} при точности {1} → {2}")
        @CsvSource({
            "7.5,   0, 8",
            "7.49,  0, 7",
            "8.8,   0, 9",
            "3.456, 2, 3.46",
            "3.454, 2, 3.45",
            "10,    0, 10"
        })
        @DisplayName("округление арифметическое поверх любого правила")
        void roundsHalfUp(String raw, int scale, String expected) {
            GradingPolicy rounded = new RoundedPolicy(TestPolicies.fixed("x", raw), scale);

            assertEquals(new BigDecimal(expected), rounded.grade(List.of()));
        }

        @Test
        @DisplayName("код остаётся прежним, к описанию добавляется точность")
        void keepsCodeAndExtendsDescription() {
            GradingPolicy inner = new FullRubricPolicy();
            GradingPolicy whole = new RoundedPolicy(inner, 0);
            GradingPolicy hundredths = new RoundedPolicy(new GradedOnlyPolicy(), 2);

            assertEquals(inner.code(), whole.code());
            assertTrue(whole.description().startsWith(inner.description()));
            assertTrue(whole.description().endsWith("до целого"));
            assertTrue(hundredths.description().contains("до 2 зн."));
        }

        @Test
        @DisplayName("оборачивает настоящую стратегию: 2,4 → 2")
        void wrapsRealStrategy() {
            assertEquals(new BigDecimal("2"),
                    new RoundedPolicy(new FullRubricPolicy(), 0).grade(withoutExam()));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, RoundedPolicy.MAX_SCALE + 1})
        @DisplayName("точность вне 0–4 — ошибка конфигурации")
        void rejectsInvalidScale(int scale) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RoundedPolicy(new FullRubricPolicy(), scale));
        }
    }
}
