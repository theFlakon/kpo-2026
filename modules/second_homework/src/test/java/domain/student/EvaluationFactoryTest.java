package domain.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.InvalidScoreException;
import domain.exception.InvalidTextException;
import domain.exception.UnknownCriterionException;
import domain.rubric.CriterionId;
import domain.rubric.Rubric;
import domain.rubric.Score;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.TestData;

@DisplayName("EvaluationFactory — единая точка создания оценки")
class EvaluationFactoryTest {

    private final Rubric rubric = TestData.rubric();

    private final EvaluationFactory factory = TestData.factory();

    @Test
    @DisplayName("оценка связана с критерием, несёт его максимум и дату из часов")
    void createsEvaluation() {
        Evaluation evaluation = factory.create(rubric, TestData.HOMEWORK, 7,
                "  молодец ");

        assertEquals(TestData.HOMEWORK, evaluation.getCriterionId());
        assertEquals(new Score(7, TestData.HOMEWORK_MAX), evaluation.getScore());
        assertEquals(TestData.TODAY, evaluation.getDate());
        assertEquals(Optional.of("молодец"), evaluation.getComment());
    }

    @Test
    @DisplayName("оценку можно выставить только по критерию из рубрикатора")
    void rejectsUnknownCriterion() {
        assertThrows(UnknownCriterionException.class,
                () -> factory.create(rubric, new CriterionId(99), 5, ""));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11, 12})
    @DisplayName("балл не выходит за пределы критерия (ДЗ — максимум 10)")
    void rejectsScoreOutOfCriterionBounds(int points) {
        assertThrows(InvalidScoreException.class,
                () -> factory.create(rubric, TestData.HOMEWORK, points, ""));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    @DisplayName("границы диапазона балла включительны")
    void acceptsBoundaryScores(int points) {
        assertEquals(points, factory.create(rubric, TestData.HOMEWORK, points,
                null).getScore().points());
    }

    @Test
    @DisplayName("максимум у каждого критерия свой: 85 — норма для экзамена")
    void maxDependsOnCriterion() {
        assertEquals(new Score(85, 100),
                factory.create(rubric, TestData.EXAM, 85, "").getScore());
    }

    @Test
    @DisplayName("комментарий необязателен, но ограничен по длине")
    void commentIsOptionalButLimited() {
        assertTrue(factory.create(rubric, TestData.EXAM, 1, "  ")
                .getComment().isEmpty());
        assertThrows(InvalidTextException.class,
                () -> factory.create(rubric, TestData.EXAM, 1,
                        "x".repeat(Evaluation.MAX_COMMENT_LENGTH + 1)));
    }

    @Test
    @DisplayName("оценка — объект-значение: одинаковые данные — равные оценки")
    void evaluationIsValueObject() {
        assertEquals(factory.create(rubric, TestData.TEST, 15, "a"),
                factory.create(rubric, TestData.TEST, 15, " a "));
    }
}
