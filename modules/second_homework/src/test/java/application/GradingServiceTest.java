package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.exception.DuplicateEvaluationException;
import domain.exception.InvalidScoreException;
import domain.exception.MissingEvaluationException;
import domain.exception.UnknownCriterionException;
import domain.exception.UnknownStudentException;
import domain.grading.GradeLine;
import domain.rubric.CriterionId;
import domain.student.Evaluation;
import domain.student.StudentId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import support.CourseFixture;
import support.TestData;

@DisplayName("GradingService — оценки")
class GradingServiceTest {

    private static final int HOMEWORK = TestData.HOMEWORK.value();

    private static final int EXAM = TestData.EXAM.value();

    private final CourseFixture course = CourseFixture.withRubric();

    private final GradingService grading = course.grading();

    private final int anna = course.setup().addStudent("Анна").id().value();

    private Optional<Evaluation> stored(int criterionId) {
        return course.students().get(new StudentId(anna))
                .evaluation(new CriterionId(criterionId));
    }

    @Nested
    @DisplayName("выставление")
    class Evaluate {

        @Test
        @DisplayName("оценка сохраняется у студента с датой из часов")
        void storesEvaluation() {
            GradeLine line = grading.evaluate(anna, HOMEWORK, 9, "хорошо");

            assertEquals("Домашние задания", line.getCriterion().getName());
            Evaluation evaluation = stored(HOMEWORK).orElseThrow();
            assertEquals(9, evaluation.getScore().points());
            assertEquals(TestData.TODAY, evaluation.getDate());
            assertEquals(Optional.of("хорошо"), evaluation.getComment());
        }

        @Test
        @DisplayName("«12 из 10» и несуществующий критерий не сохраняются")
        void invalidEvaluationIsNotStored() {
            assertThrows(InvalidScoreException.class,
                    () -> grading.evaluate(anna, HOMEWORK, 12, ""));
            assertThrows(UnknownCriterionException.class,
                    () -> grading.evaluate(anna, 99, 1, ""));

            assertTrue(course.students().get(new StudentId(anna)).evaluations().isEmpty());
        }

        @Test
        @DisplayName("вторая оценка по критерию — ошибка, первая остаётся")
        void duplicateEvaluation() {
            grading.evaluate(anna, HOMEWORK, 9, "");

            assertThrows(DuplicateEvaluationException.class,
                    () -> grading.evaluate(anna, HOMEWORK, 2, ""));
            assertEquals(9, stored(HOMEWORK).orElseThrow().getScore().points());
        }

        @Test
        @DisplayName("неизвестный студент — ошибка")
        void unknownStudent() {
            assertThrows(UnknownStudentException.class,
                    () -> grading.evaluate(42, HOMEWORK, 5, ""));
        }
    }

    @Nested
    @DisplayName("исправление и отмена")
    class CorrectAndCancel {

        @Test
        @DisplayName("исправление заменяет балл и комментарий")
        void correctsEvaluation() {
            grading.evaluate(anna, HOMEWORK, 6, "мало");

            grading.correct(anna, HOMEWORK, 10, "");

            Evaluation corrected = stored(HOMEWORK).orElseThrow();
            assertEquals(10, corrected.getScore().points());
            assertTrue(corrected.getComment().isEmpty());
        }

        @Test
        @DisplayName("исправленная оценка проходит те же проверки")
        void correctionIsValidated() {
            grading.evaluate(anna, HOMEWORK, 6, "");

            assertThrows(InvalidScoreException.class,
                    () -> grading.correct(anna, HOMEWORK, 11, ""));
            assertEquals(6, stored(HOMEWORK).orElseThrow().getScore().points());
        }

        @Test
        @DisplayName("исправить или отменить несуществующую оценку нельзя")
        void missingEvaluation() {
            assertThrows(MissingEvaluationException.class,
                    () -> grading.correct(anna, EXAM, 50, ""));
            assertThrows(MissingEvaluationException.class,
                    () -> grading.cancel(anna, EXAM));
        }

        @Test
        @DisplayName("отмена удаляет оценку и возвращает её для подтверждения")
        void cancelsEvaluation() {
            grading.evaluate(anna, EXAM, 70, "");

            GradeLine cancelled = grading.cancel(anna, EXAM);

            assertEquals(70, cancelled.getEvaluation().orElseThrow().getScore().points());
            assertTrue(stored(EXAM).isEmpty());
        }
    }

    @Test
    @DisplayName("ведомость студента — строка на каждый критерий")
    void cardShowsAllCriteria() {
        grading.evaluate(anna, EXAM, 70, "");

        StudentCard card = grading.card(anna);

        assertEquals("Анна", card.student().fullName());
        assertEquals(List.of(false, false, true),
                card.lines().stream().map(GradeLine::isGraded).toList());
    }
}
