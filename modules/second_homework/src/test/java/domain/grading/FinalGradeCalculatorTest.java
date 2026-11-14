package domain.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.EmptyRubricException;
import domain.rubric.Rubric;
import domain.rubric.Weight;
import domain.student.Student;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.TestData;
import support.TestPolicies;

/**
 * Доменный сервис расчёта итога — главный тест правила
 * «итог согласован с текущим набором оценок».
 */
@DisplayName("FinalGradeCalculator — доменный сервис итога")
class FinalGradeCalculatorTest {

    private final FinalGradeCalculator calculator = new FinalGradeCalculator();

    private final Rubric rubric = TestData.rubric();

    private final GradingPolicy full = new FullRubricPolicy();

    private static void assertGrade(String expected, FinalGrade grade) {
        assertEquals(0, new BigDecimal(expected).compareTo(grade.value()),
                () -> "ожидался итог " + expected + ", получен "
                        + grade.value().toPlainString());
    }

    @Nested
    @DisplayName("строки ведомости")
    class Lines {

        @Test
        @DisplayName("по строке на каждый критерий в порядке рубрикатора")
        void oneLinePerCriterion() {
            Student student = TestData.graded(1, 9, null, 85);

            List<GradeLine> lines = calculator.lines(rubric, student);

            assertEquals(List.of(TestData.HOMEWORK, TestData.TEST, TestData.EXAM),
                    lines.stream().map(line -> line.getCriterion().getId()).toList());
            assertEquals(List.of(true, false, true),
                    lines.stream().map(GradeLine::isGraded).toList());
        }
    }

    @Nested
    @DisplayName("расчёт")
    class Calculate {

        @Test
        @DisplayName("взвешенное среднее долей: 0,3·0,9 + 0,3·0,9 + 0,4·0,85 → 8,8")
        void weightedAverageOfRatios() {
            FinalGrade grade = calculator.calculate(rubric,
                    TestData.graded(1, 9, 18, 85), full);

            assertGrade("8.8", grade);
            assertEquals(3, grade.gradedCriteria());
            assertTrue(grade.isComplete());
        }

        @Test
        @DisplayName("веса нормируются: доли и проценты дают один итог")
        void weightsAreNormalized() {
            Rubric percents = new Rubric();
            percents.addCriterion("Домашние задания", 10, Weight.of("30"));
            percents.addCriterion("Контрольная работа", 20, Weight.of("30"));
            percents.addCriterion("Экзамен", 100, Weight.of("40"));
            Student student = TestData.graded(1, 9, 18, 85);

            assertEquals(0, calculator.calculate(rubric, student, full).value()
                    .compareTo(calculator.calculate(percents, student, full).value()));
        }

        @Test
        @DisplayName("стратегия подменяется: калькулятор отдаёт ей все строки")
        void delegatesToPolicy() {
            TestPolicies.Fixed policy = TestPolicies.fixed("fixed", "5.5");

            FinalGrade grade = calculator.calculate(rubric,
                    TestData.graded(1, 1), policy);

            assertGrade("5.5", grade);
            assertEquals(3, policy.lastLineCount());
            assertEquals(1, grade.gradedCriteria());
            assertEquals(3, grade.totalCriteria());
        }

        @Test
        @DisplayName("пустой рубрикатор — итог не из чего считать")
        void emptyRubric() {
            assertThrows(EmptyRubricException.class,
                    () -> calculator.calculate(new Rubric(), TestData.student(1), full));
        }
    }

    @Nested
    @DisplayName("согласованность с оценками")
    class Consistency {

        @Test
        @DisplayName("исправили оценку — следующий расчёт её учитывает")
        void correctionIsReflected() {
            Student student = TestData.graded(2, 6, 11, 52);
            assertGrade("5.53", calculator.calculate(rubric, student, full));

            student.replace(TestData.factory().create(rubric, TestData.HOMEWORK,
                    10, "пересдача"));

            assertGrade("6.73", calculator.calculate(rubric, student, full));
        }

        @Test
        @DisplayName("отменили оценку — критерий снова без оценки")
        void cancellationIsReflected() {
            Student student = TestData.graded(3, 4, 8, 30);
            assertGrade("3.6", calculator.calculate(rubric, student, full));

            student.cancel(TestData.EXAM);
            FinalGrade grade = calculator.calculate(rubric, student, full);

            assertGrade("2.4", grade);
            assertEquals(2, grade.gradedCriteria());
            assertFalse(grade.isComplete());
        }
    }
}
