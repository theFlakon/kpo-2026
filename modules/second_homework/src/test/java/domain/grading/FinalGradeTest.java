package domain.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.rubric.Rubric;
import domain.student.Evaluation;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

@DisplayName("FinalGrade и GradeLine — значения итога")
class FinalGradeTest {

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "10.01"})
    @DisplayName("итог вне шкалы 0–10 — ошибка в правиле, а не в данных")
    void rejectsValueOutOfScale(String value) {
        assertThrows(IllegalArgumentException.class,
                () -> new FinalGrade(new BigDecimal(value), 0, 1));
    }

    @ParameterizedTest(name = "оценено {0} из {1}")
    @CsvSource({"-1, 3", "4, 3"})
    @DisplayName("оценённых критериев не меньше 0 и не больше всех")
    void rejectsInconsistentCounts(int graded, int total) {
        assertThrows(IllegalArgumentException.class,
                () -> new FinalGrade(BigDecimal.ONE, graded, total));
    }

    @ParameterizedTest(name = "{0} → положительный: {1}")
    @CsvSource({"3.99, false", "4, true", "4.00, true", "10, true", "0, false"})
    @DisplayName("положительный итог — от 4 по шкале ВШЭ")
    void passingThreshold(String value, boolean passing) {
        assertEquals(passing, new FinalGrade(new BigDecimal(value), 1, 1).isPassing());
    }

    @Test
    @DisplayName("ведомость полная, когда оценены все критерии")
    void completeness() {
        assertTrue(new FinalGrade(BigDecimal.ONE, 3, 3).isComplete());
        assertFalse(new FinalGrade(BigDecimal.ONE, 2, 3).isComplete());
    }

    @Test
    @DisplayName("строка ведомости: оценка обязана относиться к своему критерию")
    void gradeLineChecksCriterion() {
        Rubric rubric = TestData.rubric();
        Evaluation homework = TestData.factory().create(rubric, TestData.HOMEWORK, 5, "");

        assertThrows(IllegalArgumentException.class,
                () -> GradeLine.graded(rubric.criterion(TestData.EXAM), homework));
        assertTrue(GradeLine.graded(rubric.criterion(TestData.HOMEWORK), homework)
                .isGraded());
        assertTrue(GradeLine.ungraded(rubric.criterion(TestData.EXAM))
                .getEvaluation().isEmpty());
    }
}
